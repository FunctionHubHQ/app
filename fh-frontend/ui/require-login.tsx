'use client'
import React from 'react';
import {useAuthContext} from "#/context/AuthContext";
import {useRouter} from "next/navigation";

export default function RequireLogin({
  children,
}: {
  children: React.ReactNode;
}) {
  const {user} = useAuthContext()
  const router = useRouter()
  if (!user) {
    router.push("/login")
  }
  return (
    <>{children}</>
  );
}
