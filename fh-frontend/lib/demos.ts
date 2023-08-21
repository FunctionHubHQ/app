export type Item = {
  name: string;
  slug: string;
  description?: string;
};

export const demos: { description?: string; name: string; items: Item[] }[] = [
  {
    name: 'Demo',
    items: []
  },
  {
    name: 'Account',
    description: 'Lorem ipsum is placeholder text commonly used in the graphic, print, and publishing industries for previewing layouts and visual mockups Lorem ipsum is placeholder text commonly used in the graphic, print, and publishing industries for previewing layouts.',
    items: [
      {
        name: 'get_current_time_and_weather',
        slug: 'profile',
        description: 'Lorem ipsum is placeholder text commonly used in the graphic, print, and publishing industries for previewing layouts and visual mockups Lorem ipsum is placeholder text commonly used in the graphic, print, and publishing industries for previewing layouts',
      },
      {
        name: 'summary_any_website',
        slug: 'api-keys',
        description: 'Lorem ipsum is placeholder text commonly used in the graphic, print, and publishing industries for previewing layouts and visual mockups',
      },
      {
        name: 'turn_html_tables_into_json_object_every_night_on_mondays',
        slug: 'api-keys',
        description: 'Lorem ipsum is placeholder ',
      },
      {
        name: 'get_next_available_flight',
        slug: 'api-keys',
        description: 'Lorem ipsum is placeholder text commonly used in the graphic, print, and publishing industries for previewing layouts and visual mockups Lorem ipsum is placeholder text commonly used in the graphic, print, and publishing industries for previewing layouts',
      },
    ],
  },
  {
    name: 'Projects',
    items: [
      {
        name: 'Projects',
        slug: 'projects',
        description: 'View all your projects',
      },
      {
        name: 'Explore',
        slug: 'explore',
        description: 'Explore public functions',
      },
      {
        name: 'Docs',
        slug: 'docs',
        description: 'FunctionHub documentation',
      },
      {
        name: 'Tutorials',
        slug: 'tutorials',
        description: 'Learn how to create functions',
      },
    ],
  },
  {
    name: 'Company',
    items: [
      {
        name: 'About',
        slug: 'about',
        description: 'About FunctionHub',
      },
      {
        name: 'Terms',
        slug: 'terms',
        description: 'Terms of use',
      },
      {
        name: 'Privacy',
        slug: 'privacy',
        description: 'Privacy policy',
      },
    ],
  },
  // {
  //   name: 'Layouts',
  //   items: [
  //     {
  //       name: 'Nested Layouts',
  //       slug: 'layouts',
  //       description: 'Create UI that is shared across routes',
  //     },
  //     {
  //       name: 'Grouped Layouts',
  //       slug: 'route-groups',
  //       description: 'Organize routes without affecting URL paths',
  //     },
  //     {
  //       name: 'Parallel Routes',
  //       slug: 'parallel-routes',
  //       description: 'Render multiple pages in the same layout',
  //     },
  //   ],
  // },
  // {
  //   name: 'File Conventions',
  //   items: [
  //     {
  //       name: 'Loading',
  //       slug: 'loading',
  //       description:
  //         'Create meaningful Loading UI for specific parts of an app',
  //     },
  //     {
  //       name: 'Error',
  //       slug: 'error-handling',
  //       description: 'Create Error UI for specific parts of an app',
  //     },
  //     {
  //       name: 'Not Found',
  //       slug: 'not-found',
  //       description: 'Create Not Found UI for specific parts of an app',
  //     },
  //   ],
  // },
  // {
  //   name: 'Data Fetching',
  //   items: [
  //     {
  //       name: 'Streaming with Suspense',
  //       slug: 'streaming',
  //       description:
  //         'Streaming data fetching from the server with React Suspense',
  //     },
  //     {
  //       name: 'Static Data',
  //       slug: 'ssg',
  //       description: 'Generate static pages',
  //     },
  //     {
  //       name: 'Dynamic Data',
  //       slug: 'ssr',
  //       description: 'Server-render pages',
  //     },
  //     {
  //       name: 'Incremental Static Regeneration',
  //       slug: 'isr',
  //       description: 'Get the best of both worlds between static & dynamic',
  //     },
  //   ],
  // },
  // {
  //   name: 'Components',
  //   items: [
  //     {
  //       name: 'Client Context',
  //       slug: 'context',
  //       description:
  //         'Pass context between Client Components that cross Server/Client Component boundary',
  //     },
  //   ],
  // },
  // {
  //   name: 'Misc',
  //   items: [
  //     {
  //       name: 'Client Component Hooks',
  //       slug: 'hooks',
  //       description: 'Preview the routing hooks available in Client Components',
  //     },
  //     {
  //       name: 'CSS and CSS-in-JS',
  //       slug: 'styling',
  //       description: 'Preview the supported styling solutions',
  //     },
  //     {
  //       name: 'Code Snippets',
  //       slug: 'snippets',
  //       description: 'A collection of useful App Router code snippets',
  //     },
  //   ],
  // },
];
