import CodeEditor from "#/ui/editor/editor";

export default async function Page({
  params,
}: {
  params: { categorySlug: string };
}) {

  return (
    <div className="w-full">
      <CodeEditor/>
    </div>
  );
}
