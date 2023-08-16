import { getCategory } from '#/app/api/categories/getCategories';
import { SkeletonCard } from '#/ui/skeleton-card';
import SandboxTabs from "#/ui/interactive-api/sandboxTabs";
import CodeEditor from "#/ui/editor/editor";

export default async function Page({
  params,
}: {
  params: { categorySlug: string };
}) {
  const category = await getCategory({ slug: params.categorySlug });

  return (
    <div className="w-full">
      {/*<h1 className="text-xl font-medium text-gray-400/80">*/}
      {/*  All {category.name}*/}
      {/*</h1>*/}

      {/*<div className="grid grid-cols-1 gap-6 lg:grid-cols-3">*/}
      {/*  {Array.from({ length: 9 }).map((_, i) => (*/}
      {/*    <SkeletonCard key={i} />*/}
      {/*  ))}*/}
      {/*</div>*/}
      <CodeEditor/>
    </div>
  );
}
