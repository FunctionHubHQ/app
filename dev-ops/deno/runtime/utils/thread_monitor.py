import subprocess
import time
import os


class DenoWorkerThread:
  def __init__(self, name=None, cpu_time=0, memory_usage=0, ppid=0, tid=0):
    self.name = name  # Name of the Deno worker thread
    self.cpu_time = cpu_time  # CPU time used by the thread
    self.memory_usage = memory_usage  # Memory usage of the thread
    self.ppid = ppid  # Parent process ID of the thread
    self.tid = tid  # Thread ID of the thread
    self.started_at = time.time_ns() // 1000000  # +/- 5 milliseconds


def __str__(self):
  return f"Name: {self.name}, CPU Time: {self.cpu_time}, Memory Usage: {self.memory_usage}, PPID: {self.ppid}, TID: {self.tid}"


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

  with open(stat_path, "r") as stat_file:
    data = stat_file.read().split()
    utime_ticks = int(
        data[13])  # 14th field: utime (user mode time in clock ticks)
    stime_ticks = int(
        data[14])  # 15th field: stime (kernel mode time in clock ticks)
    clock_ticks_per_second = os.sysconf(os.sysconf_names['SC_CLK_TCK'])
    utime_mseconds = (utime_ticks / clock_ticks_per_second) * 1000
    stime_mseconds = (stime_ticks / clock_ticks_per_second) * 1000
    cpu_time = utime_mseconds + stime_mseconds

  thread.tid = worker_pid
  thread.memory_usage = memory_usage
  thread.cpu_time = cpu_time
  thread.ppid = ppid
  thread.name = name

  return thread


def alert_on_threshold(thread):
  """Raise an alarm by signaling the Deno runtime that a worker thread has exceeded
  resource limitations"""
  # print("About to alert...")
  pass

if __name__ == "__main__":
  print("Thread monitor is running...")
  sleep_duration = 0.005  # Sample every 5 milliseconds
  active_worker_threads = {}
  while True:
    # Fetch the pid every time since deno could have died and restarted
    deno_pid = get_pids("deno run --allow-net")[0]
    worker_pids = get_pids("{worker-")
    for worker_pid in worker_pids:
      thread = get_thread_usage_metrics(deno_pid, worker_pid)
      if thread.tid not in active_worker_threads:
        print(f"Name: {thread.name}, CPU Time: {thread.cpu_time}, Memory Usage: {thread.memory_usage}, PPID: {thread.ppid}, TID: {thread.tid}, Started At: {thread.started_at}")
      else:
        thread.started_at = active_worker_threads.get(thread.tid).started_at

      active_worker_threads[thread.tid] = thread

      # Check thread usage limits and raise an alarm if any of them exceed the threshold
      alert_on_threshold(thread)

    # Remove any workers that have finished or exited
    updated_threads = {key: value for key, value in
                       active_worker_threads.items() if key in worker_pids}

    # Sample every 5 milliseconds even though the default Kernel clock tick
    # rate is 100hz, i.e. 1 tick every 10 milliseconds
    time.sleep(sleep_duration)