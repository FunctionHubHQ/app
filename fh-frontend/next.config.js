/** @type {import('next').NextConfig} */
const DOC_URL = "http://localhost:3001"
const nextConfig = {
  reactStrictMode: true, // Recommended for the `pages` directory, default in `app`.
  experimental: {
    appDir: true,
  },
  async rewrites() {
    return [
      {
        source: '/:path*',
        destination: `/:path*`,
      },
      {
        source: '/docs',
        destination: `${DOC_URL}/docs`,
      },
      {
        source: '/docs/:path*',
        destination: `${DOC_URL}/docs/:path*`,
      },
    ]
  },
};

module.exports = nextConfig;
