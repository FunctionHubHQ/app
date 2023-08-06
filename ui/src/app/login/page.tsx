'use client'
import * as React from "react";
import Login from "@/components/login";
import {useAuthContext} from "@/context/AuthContext";
import {useEffect} from "react";
import {useRouter} from "next/navigation";

function Index() {
  // @ts-ignore
  const {user} = useAuthContext()
  const router = useRouter()

  useEffect(() => {
    if (user) router.push("/")
  }, [user])
  return <Login/>
}
export default Index;