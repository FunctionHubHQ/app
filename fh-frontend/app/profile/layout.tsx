import React from 'react';
import RequireLogin from "#/ui/require-login";

export default async function Layout({
  children,
}: {
  children: React.ReactNode;
}) {


  return (
    <div className="space-y-9">
      <div><RequireLogin>{children}</RequireLogin></div>
    </div>
  );
}
