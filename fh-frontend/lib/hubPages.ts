export type Item = {
  name: string;
  slug: string;
  description?: string;
};

export const hubPages: { description?: string; name: string; items: Item[] }[] = [
  {
    name: 'My Hub',
    items: [
      {
        name: 'Projects',
        slug: 'hub/projects',
        description: 'View all your projects',
      },
      {
        name: 'Explore',
        slug: 'explore',
        description: 'Explore public functions and integrations',
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
    name: 'Manage',
    items: [
      {
        name: 'API Keys',
        slug: 'hub/api-keys',
        description: 'Manage FunctionHub and OpenAI API keys',
      },
      {
        name: 'Profile',
        slug: 'hub/profile',
        description: 'Profile',
      }
    ],
  }
];
