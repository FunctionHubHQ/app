import os
import time
import uuid
import json
import argparse
import subprocess
import http.client


class DenoWorkerThread:
  def __init__(self, name=None, cpu_time=0, memory_usage=0, ppid=0, tid=0):
    self.id = str(uuid.uuid4())
    self.name = name  # Name of the Deno worker thread
    self.curr_cpu_time = cpu_time  # CPU time used by the thread
    self.prev_cpu_time = 0
    self.curr_memory_usage = memory_usage  # Memory usage of the thread
    self.prev_memory_usage = 0
    self.ppid = ppid  # Parent process ID of the thread
    self.tid = tid  # Thread ID of the thread
    self.created_at = time.time_ns() // 1000000  # +/- 5 milliseconds
    self.updated_at = time.time_ns() // 1000000  # +/- 5 milliseconds


def __str__(self):
  return f"ID: {self.id}, Name: {self.name}, CPU Time: {self.curr_cpu_time}, Memory Usage: {self.curr_memory_usage}, PPID: {self.ppid}, TID: {self.tid}"


def request(path, body):
  # Define the target host and path
  host = "localhost:8000"
  conn = http.client.HTTPConnection(host)
  headers = {
    "Content-Type": "application/json",
  }
  conn.request("POST", path, json.dumps(body), headers)
  conn.getresponse()
  conn.close()


def get_pids(prefix):
  # Run the ps command to list processes with "deno" in their name
  ps_output = ""
  try:
    ps_output = subprocess.check_output(["ps", "-T"])
  except subprocess.CalledProcessError as e:
    print(f"Error running ps command: {e}")

  # Split the output into lines and skip the header line
  ps_lines = ps_output.decode("utf-8").splitlines()[1:]

  pids = []
  for line in ps_lines:
    tokens = [t for t in line.strip().split(' ') if t]
    cmd = " ".join(tokens[3:])
    pid = int(tokens[0])

    # Check if the process name contains "deno"
    deno_cmd = "deno run --allow-net"
    if prefix in cmd and cmd.startswith(prefix) and deno_cmd in cmd:
      pids.append(pid)

  return pids


def get_thread_usage_metrics(ppid, tid) -> DenoWorkerThread:
  """Read thread memory usage and cpu time"""
  status_path = f"/proc/{ppid}/task/{tid}/status"
  stat_path = f"/proc/{ppid}/task/{tid}/stat"
  thread = DenoWorkerThread()
  memory_usage = 0
  cpu_time = 0
  name = ""
  try:
    with open(status_path, "r") as status_file:
      lines = status_file.readlines()
      for line in lines:
        if line.startswith("VmRSS:"):
          tokens = [t for t in line.split("VmRSS:")[-1].split(" ")]
          for t in tokens:
            try:
              memory_usage = int(t) * 1000  # Normalize memory to bytes
              break
            except ValueError:
              pass

        elif line.startswith("Name:"):
          tokens = [t for t in line.split("Name:") if t]
          name = tokens[0]
  except Exception as e:
    now = time.time_ns() // 1000000
    print(f"Status error on TID {tid}: {now}")

  try:
    with open(stat_path, "r") as stat_file:
      data = stat_file.read().split()
      utime_ticks = int(
          data[13])  # 14th field: utime (user mode time in clock ticks)
      clock_ticks_per_second = os.sysconf(os.sysconf_names['SC_CLK_TCK'])
      utime_mseconds = (utime_ticks / clock_ticks_per_second) * 1000
      cpu_time = utime_mseconds
  except Exception as e:
    now = time.time_ns() // 1000000
    print(f"Stat error on TID {tid}: {now}")

  thread.tid = tid
  thread.ppid = ppid
  thread.curr_memory_usage = memory_usage
  thread.curr_cpu_time = cpu_time
  thread.name = name
  thread.updated_at = time.time_ns() // 1000000

  return thread


def alert_on_metric_change(thread):
  """Send an alert if there is any usage change in CPU or memory"""
  if thread.curr_cpu_time > thread.prev_cpu_time:
    request("/thread-alarm", {
      "thread_id": thread.id,
      "tid": thread.tid,
      "cpu_time": thread.curr_cpu_time,
      "updated_at": thread.updated_at
    })

  if thread.curr_memory_usage > thread.prev_memory_usage:
    request("/thread-alarm", {
      "thread_id": thread.id,
      "tid": thread.tid,
      "memory_usage": thread.curr_memory_usage,
      "updated_at": thread.updated_at
    })


def signal_on_new_thread(thread):
  """Signal the Deno runtime that a new thread has been detected.
  The runtime should use the thread ID to keep track of future alarms."""
  request("/new-thread", {
    "thread_id": thread.id,
    "created_at": thread.created_at,
    "ppid": thread.ppid,
    "tid": thread.tid
  })
  print("New thread detected: ", thread.tid)


def start_monitor(sampling_rate=0.005):
  """Start monitoring Deno worker threads
  :param max_cpu_time max allowed cpu time in milliseconds for a thread (best estimate)
  :param max_memory_usage max allowed memory usage in bytes. Default is 128MB
  :param sampling_rate how often the threads should be scanned
  """
  print(f"Thread monitor up: sampling_rate={sampling_rate}")
  active_worker_threads = {}
  while True:
    # try:
    # Fetch the pid every time since deno could have died and restarted
    deno_pid = get_pids("deno run --allow-net")[0]
    worker_pids = get_pids("{worker-")
    for worker_pid in worker_pids:
      try:
        thread = get_thread_usage_metrics(deno_pid, worker_pid)
        if thread.tid not in active_worker_threads:
          print(
              f"PPID: {thread.ppid}, TID: {thread.tid}, CPU Time: {thread.curr_cpu_time}, Memory Usage: {thread.curr_memory_usage}")
          active_worker_threads[thread.tid] = thread
          signal_on_new_thread(thread)
        else:
          prev_thread = active_worker_threads.get(thread.tid)
          prev_thread.prev_cpu_time = prev_thread.curr_cpu_time
          prev_thread.prev_memory_usage = prev_thread.curr_memory_usage
          prev_thread.curr_cpu_time = thread.curr_cpu_time
          prev_thread.curr_memory_usage = thread.curr_memory_usage
          prev_thread.updated_at = thread.updated_at

          alert_on_metric_change(prev_thread)
      except Exception as e:
        print(f"Encountered an error: {e}")

    # Remove any workers that have finished or exited
    updated_threads = {key: value for key, value in
                       active_worker_threads.items() if key in worker_pids}
    active_worker_threads = updated_threads
  # except Exception as e:
  #   print(e)

    # Sample every 5 milliseconds even though the default Kernel clock tick
    # rate is 100hz, i.e. 1 tick every 10 milliseconds
    time.sleep(sampling_rate)


if __name__ == "__main__":
  parser = argparse.ArgumentParser(description="Deno Worker Thread Monitor")
  # parser.add_argument("max_cpu_time", type=int,
  #                     help="Max CPU time in milliseconds")
  # parser.add_argument("max_memory_usage", type=int,
  #                     help="Max memory usae in bytes")
  parser.add_argument("--sampling_rate", type=float, default=0.005,
                      help="Sampling frequency")
  args = parser.parse_args()

  # max_cpu_time = args.max_cpu_time
  # max_memory_usage = args.max_memory_usage
  sampling_rate = args.sampling_rate
  start_monitor(sampling_rate=sampling_rate)
