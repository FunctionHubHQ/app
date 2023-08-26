import Link from 'next/link';
import React from "react";
import FHForkButton from "#/ui/project/fh-fork-button";
import Pill from "#/ui/pill";
import Pagination from "#/ui/pagination";

const items = [
  {
    name: 'Updating searchParams ',
    slug: 'search-params',
    description: 'Enterprise SaaS Starter Kit - Kickstart your enterprise app development with the Next.js SaaS boilerplate',
  },
];

export default function Page() {
  return (
        <div className="min-h-screen pt-20">
          <div className="mx-auto max-w-4xl space-y-8 px-2 pt-20 lg:px-8 lg:py-8">
            <div className="bg-vc-border-gradient rounded-lg p-px shadow-lg shadow-black/20">
              <div className="rounded-lg bg-black p-3.5 lg:p-6">
                <div className="grid grid-cols-1 grid-rows-1 md:grid-cols-2 lg:grid-cols-2 gap-1">
                  <div className="flex items-center">
                    <img className="w-10 h-10 rounded-full"
                         src="https://placehold.co/200x200"
                         alt="Code owner"/>
                    <span className="pl-2">JSLancerTeam</span>
                  </div>
                  <div className="flex justify-end">
                    <FHForkButton forkCount={0}/>
                  </div>
                </div>

                <div className="space-y-6 mt-6">
                  <div className="grid grid-cols-1 gap-5 lg:grid-cols-1 mt-4">
                    {items.map((item) => {
                      return (
                          <Link
                              href={`/snippets/${item.slug}`}
                              key={item.name}
                              className="group block space-y-1.5 rounded-lg bg-gray-900 px-5 py-3 hover:bg-gray-800"
                          >
                            <div className="font-medium text-gray-200 group-hover:text-gray-50">
                              {item.name}
                            </div>

                            {item.description ? (
                                <div className="line-clamp-3 text-sm text-gray-400 group-hover:text-gray-300">
                                  {item.description}
                                </div>
                            ) : null}
                          </Link>
                      );
                    })}
                  </div>
                    <ul className="flex flex-wrap">
                      <li className="ml-2 mr-2 mb-4"><Pill label={'sass'}/></li>
                      <li className="ml-2 mr-2 mb-4"><Pill label={'loremipsum'}/></li>
                      <li className="ml-2 mr-2 mb-4"><Pill label={'microsoft'}/></li>
                      <li className="ml-2 mr-2 mb-4"><Pill label={'gamma'}/></li>
                      <li className="ml-2 mr-2 mb-4"><Pill label={'beta'}/></li>
                      <li className="ml-2 mr-2 mb-4"><Pill label={'alpha'}/></li>
                      <li className="ml-2 mr-2 mb-4"><Pill label={'sass'}/></li>
                      <li className="ml-2 mr-2 mb-4"><Pill label={'starterkit'}/></li>
                      <li className="ml-2 mr-2 mb-4"><Pill label={'sass'}/></li>
                      <li className="ml-2 mr-2 mb-4"><Pill label={'nextjs'}/></li>
                      <li className="ml-2 mr-2 mb-4"><Pill label={'tailwind'}/></li>
                      <li className="ml-2 mr-2 mb-4"><Pill label={'boilerplate'}/></li>
                      <li className="ml-2 mr-2 mb-4"><Pill label={'typescript'}/></li>
                      <li className="ml-2 mr-2 mb-4"><Pill label={'nodejs'}/></li>
                      <li className="ml-2 mr-2 mb-4"><Pill label={'nodejs'}/></li>
                      <li className="ml-2 mr-2 mb-4"><Pill label={'nodejs'}/></li>
                      <li className="ml-2 mr-2 mb-4"><Pill label={'nodejs'}/></li>
                      <li className="ml-2 mr-2 mb-4"><Pill label={'nodejs-is-the-best-thing-to-happen'}/></li>
                      <li className="ml-2 mr-2 mb-4"><Pill label={'hello world what is your name'}/></li>
                    </ul>
                </div>
              </div>
            </div>

            <div className="bg-vc-border-gradient rounded-lg p-px shadow-lg shadow-black/20">
              <div className="rounded-lg bg-black p-3.5 lg:p-6">
                <div className="grid grid-cols-1 grid-rows-1 md:grid-cols-2 lg:grid-cols-2 gap-1">
                  <div className="flex items-center">
                    <img className="w-10 h-10 rounded-full"
                         src="https://placehold.co/200x200"
                         alt="Code owner"/>
                    <span className="pl-2">JSLancerTeam</span>
                  </div>
                  <div className="flex justify-end">
                    <FHForkButton forkCount={0}/>
                  </div>
                </div>

                <div className="space-y-6 mt-6">
                  <div className="grid grid-cols-1 gap-5 lg:grid-cols-1 mt-4">
                    {items.map((item) => {
                      return (
                          <Link
                              href={`/snippets/${item.slug}`}
                              key={item.name}
                              className="group block space-y-1.5 rounded-lg bg-gray-900 px-5 py-3 hover:bg-gray-800"
                          >
                            <div className="font-medium text-gray-200 group-hover:text-gray-50">
                              {item.name}
                            </div>

                            {item.description ? (
                                <div className="line-clamp-3 text-sm text-gray-400 group-hover:text-gray-300">
                                  {item.description}
                                </div>
                            ) : null}
                          </Link>
                      );
                    })}
                  </div>
                  <ul className="flex flex-wrap">
                    <li className="ml-2 mr-2 mb-4"><Pill label={'sass'}/></li>
                    <li className="ml-2 mr-2 mb-4"><Pill label={'loremipsum'}/></li>
                    <li className="ml-2 mr-2 mb-4"><Pill label={'microsoft'}/></li>
                    <li className="ml-2 mr-2 mb-4"><Pill label={'gamma'}/></li>
                    <li className="ml-2 mr-2 mb-4"><Pill label={'beta'}/></li>
                    <li className="ml-2 mr-2 mb-4"><Pill label={'alpha'}/></li>
                    <li className="ml-2 mr-2 mb-4"><Pill label={'sass'}/></li>
                    <li className="ml-2 mr-2 mb-4"><Pill label={'starterkit'}/></li>
                    <li className="ml-2 mr-2 mb-4"><Pill label={'sass'}/></li>
                    <li className="ml-2 mr-2 mb-4"><Pill label={'nextjs'}/></li>
                    <li className="ml-2 mr-2 mb-4"><Pill label={'tailwind'}/></li>
                    <li className="ml-2 mr-2 mb-4"><Pill label={'boilerplate'}/></li>
                    <li className="ml-2 mr-2 mb-4"><Pill label={'typescript'}/></li>
                    <li className="ml-2 mr-2 mb-4"><Pill label={'nodejs'}/></li>
                    <li className="ml-2 mr-2 mb-4"><Pill label={'nodejs'}/></li>
                    <li className="ml-2 mr-2 mb-4"><Pill label={'nodejs'}/></li>
                    <li className="ml-2 mr-2 mb-4"><Pill label={'nodejs'}/></li>
                    <li className="ml-2 mr-2 mb-4"><Pill label={'nodejs-is-the-best-thing-to-happen'}/></li>
                    <li className="ml-2 mr-2 mb-4"><Pill label={'hello world what is your name'}/></li>
                  </ul>
                </div>
              </div>
            </div>

            {/*<div className="bg-vc-border-gradient rounded-lg p-px shadow-lg shadow-black/20">*/}
            {/*  <div className="rounded-lg bg-black p-3.5 lg:p-6">*/}
            {/*    <div className="grid grid-cols-1 grid-rows-1 md:grid-cols-2 lg:grid-cols-2 gap-1">*/}
            {/*      <div className="flex items-center">*/}
            {/*        <img className="w-10 h-10 rounded-full"*/}
            {/*             src="https://placehold.co/200x200"*/}
            {/*             alt="Code owner"/>*/}
            {/*        <span className="pl-2">JSLancerTeam</span>*/}
            {/*      </div>*/}
            {/*      <div className="flex justify-end">*/}
            {/*        <FHForkButton forkCount={0}/>*/}
            {/*      </div>*/}
            {/*    </div>*/}

            {/*    <div className="space-y-6 mt-6">*/}
            {/*      <div className="grid grid-cols-1 gap-5 lg:grid-cols-1 mt-4">*/}
            {/*        {items.map((item) => {*/}
            {/*          return (*/}
            {/*              <Link*/}
            {/*                  href={`/snippets/${item.slug}`}*/}
            {/*                  key={item.name}*/}
            {/*                  className="group block space-y-1.5 rounded-lg bg-gray-900 px-5 py-3 hover:bg-gray-800"*/}
            {/*              >*/}
            {/*                <div className="font-medium text-gray-200 group-hover:text-gray-50">*/}
            {/*                  {item.name}*/}
            {/*                </div>*/}

            {/*                {item.description ? (*/}
            {/*                    <div className="line-clamp-3 text-sm text-gray-400 group-hover:text-gray-300">*/}
            {/*                      {item.description}*/}
            {/*                    </div>*/}
            {/*                ) : null}*/}
            {/*              </Link>*/}
            {/*          );*/}
            {/*        })}*/}
            {/*      </div>*/}
            {/*      <ul className="flex flex-wrap">*/}
            {/*        <li className="ml-2 mr-2 mb-4"><Pill label={'sass'}/></li>*/}
            {/*        <li className="ml-2 mr-2 mb-4"><Pill label={'loremipsum'}/></li>*/}
            {/*        <li className="ml-2 mr-2 mb-4"><Pill label={'microsoft'}/></li>*/}
            {/*        <li className="ml-2 mr-2 mb-4"><Pill label={'gamma'}/></li>*/}
            {/*        <li className="ml-2 mr-2 mb-4"><Pill label={'beta'}/></li>*/}
            {/*        <li className="ml-2 mr-2 mb-4"><Pill label={'alpha'}/></li>*/}
            {/*        <li className="ml-2 mr-2 mb-4"><Pill label={'sass'}/></li>*/}
            {/*        <li className="ml-2 mr-2 mb-4"><Pill label={'starterkit'}/></li>*/}
            {/*        <li className="ml-2 mr-2 mb-4"><Pill label={'sass'}/></li>*/}
            {/*        <li className="ml-2 mr-2 mb-4"><Pill label={'nextjs'}/></li>*/}
            {/*        <li className="ml-2 mr-2 mb-4"><Pill label={'tailwind'}/></li>*/}
            {/*        <li className="ml-2 mr-2 mb-4"><Pill label={'boilerplate'}/></li>*/}
            {/*        <li className="ml-2 mr-2 mb-4"><Pill label={'typescript'}/></li>*/}
            {/*        <li className="ml-2 mr-2 mb-4"><Pill label={'nodejs'}/></li>*/}
            {/*        <li className="ml-2 mr-2 mb-4"><Pill label={'nodejs'}/></li>*/}
            {/*        <li className="ml-2 mr-2 mb-4"><Pill label={'nodejs'}/></li>*/}
            {/*        <li className="ml-2 mr-2 mb-4"><Pill label={'nodejs'}/></li>*/}
            {/*        <li className="ml-2 mr-2 mb-4"><Pill label={'nodejs-is-the-best-thing-to-happen'}/></li>*/}
            {/*        <li className="ml-2 mr-2 mb-4"><Pill label={'hello world what is your name'}/></li>*/}
            {/*      </ul>*/}
            {/*    </div>*/}
            {/*  </div>*/}
            {/*</div>*/}

            {/*<div className="bg-vc-border-gradient rounded-lg p-px shadow-lg shadow-black/20">*/}
            {/*  <div className="rounded-lg bg-black p-3.5 lg:p-6">*/}
            {/*    <div className="grid grid-cols-1 grid-rows-1 md:grid-cols-2 lg:grid-cols-2 gap-1">*/}
            {/*      <div className="flex items-center">*/}
            {/*        <img className="w-10 h-10 rounded-full"*/}
            {/*             src="https://placehold.co/200x200"*/}
            {/*             alt="Code owner"/>*/}
            {/*        <span className="pl-2">JSLancerTeam</span>*/}
            {/*      </div>*/}
            {/*      <div className="flex justify-end">*/}
            {/*        <FHForkButton forkCount={0}/>*/}
            {/*      </div>*/}
            {/*    </div>*/}

            {/*    <div className="space-y-6 mt-6">*/}
            {/*      <div className="grid grid-cols-1 gap-5 lg:grid-cols-1 mt-4">*/}
            {/*        {items.map((item) => {*/}
            {/*          return (*/}
            {/*              <Link*/}
            {/*                  href={`/snippets/${item.slug}`}*/}
            {/*                  key={item.name}*/}
            {/*                  className="group block space-y-1.5 rounded-lg bg-gray-900 px-5 py-3 hover:bg-gray-800"*/}
            {/*              >*/}
            {/*                <div className="font-medium text-gray-200 group-hover:text-gray-50">*/}
            {/*                  {item.name}*/}
            {/*                </div>*/}

            {/*                {item.description ? (*/}
            {/*                    <div className="line-clamp-3 text-sm text-gray-400 group-hover:text-gray-300">*/}
            {/*                      {item.description}*/}
            {/*                    </div>*/}
            {/*                ) : null}*/}
            {/*              </Link>*/}
            {/*          );*/}
            {/*        })}*/}
            {/*      </div>*/}
            {/*      <ul className="flex flex-wrap">*/}
            {/*        <li className="ml-2 mr-2 mb-4"><Pill label={'sass'}/></li>*/}
            {/*        <li className="ml-2 mr-2 mb-4"><Pill label={'loremipsum'}/></li>*/}
            {/*        <li className="ml-2 mr-2 mb-4"><Pill label={'microsoft'}/></li>*/}
            {/*        <li className="ml-2 mr-2 mb-4"><Pill label={'gamma'}/></li>*/}
            {/*        <li className="ml-2 mr-2 mb-4"><Pill label={'beta'}/></li>*/}
            {/*        <li className="ml-2 mr-2 mb-4"><Pill label={'alpha'}/></li>*/}
            {/*        <li className="ml-2 mr-2 mb-4"><Pill label={'sass'}/></li>*/}
            {/*        <li className="ml-2 mr-2 mb-4"><Pill label={'starterkit'}/></li>*/}
            {/*        <li className="ml-2 mr-2 mb-4"><Pill label={'sass'}/></li>*/}
            {/*        <li className="ml-2 mr-2 mb-4"><Pill label={'nextjs'}/></li>*/}
            {/*        <li className="ml-2 mr-2 mb-4"><Pill label={'tailwind'}/></li>*/}
            {/*        <li className="ml-2 mr-2 mb-4"><Pill label={'boilerplate'}/></li>*/}
            {/*        <li className="ml-2 mr-2 mb-4"><Pill label={'typescript'}/></li>*/}
            {/*        <li className="ml-2 mr-2 mb-4"><Pill label={'nodejs'}/></li>*/}
            {/*        <li className="ml-2 mr-2 mb-4"><Pill label={'nodejs'}/></li>*/}
            {/*        <li className="ml-2 mr-2 mb-4"><Pill label={'nodejs'}/></li>*/}
            {/*        <li className="ml-2 mr-2 mb-4"><Pill label={'nodejs'}/></li>*/}
            {/*        <li className="ml-2 mr-2 mb-4"><Pill label={'nodejs-is-the-best-thing-to-happen'}/></li>*/}
            {/*        <li className="ml-2 mr-2 mb-4"><Pill label={'hello world what is your name'}/></li>*/}
            {/*      </ul>*/}
            {/*    </div>*/}
            {/*  </div>*/}
            {/*</div>*/}

            {/*<div className="bg-vc-border-gradient rounded-lg p-px shadow-lg shadow-black/20">*/}
            {/*  <div className="rounded-lg bg-black p-3.5 lg:p-6">*/}
            {/*    <div className="grid grid-cols-1 grid-rows-1 md:grid-cols-2 lg:grid-cols-2 gap-1">*/}
            {/*      <div className="flex items-center">*/}
            {/*        <img className="w-10 h-10 rounded-full"*/}
            {/*             src="https://placehold.co/200x200"*/}
            {/*             alt="Code owner"/>*/}
            {/*        <span className="pl-2">JSLancerTeam</span>*/}
            {/*      </div>*/}
            {/*      <div className="flex justify-end">*/}
            {/*        <FHForkButton forkCount={0}/>*/}
            {/*      </div>*/}
            {/*    </div>*/}

            {/*    <div className="space-y-6 mt-6">*/}
            {/*      <div className="grid grid-cols-1 gap-5 lg:grid-cols-1 mt-4">*/}
            {/*        {items.map((item) => {*/}
            {/*          return (*/}
            {/*              <Link*/}
            {/*                  href={`/snippets/${item.slug}`}*/}
            {/*                  key={item.name}*/}
            {/*                  className="group block space-y-1.5 rounded-lg bg-gray-900 px-5 py-3 hover:bg-gray-800"*/}
            {/*              >*/}
            {/*                <div className="font-medium text-gray-200 group-hover:text-gray-50">*/}
            {/*                  {item.name}*/}
            {/*                </div>*/}

            {/*                {item.description ? (*/}
            {/*                    <div className="line-clamp-3 text-sm text-gray-400 group-hover:text-gray-300">*/}
            {/*                      {item.description}*/}
            {/*                    </div>*/}
            {/*                ) : null}*/}
            {/*              </Link>*/}
            {/*          );*/}
            {/*        })}*/}
            {/*      </div>*/}
            {/*      <ul className="flex flex-wrap">*/}
            {/*        <li className="ml-2 mr-2 mb-4"><Pill label={'sass'}/></li>*/}
            {/*        <li className="ml-2 mr-2 mb-4"><Pill label={'loremipsum'}/></li>*/}
            {/*        <li className="ml-2 mr-2 mb-4"><Pill label={'microsoft'}/></li>*/}
            {/*        <li className="ml-2 mr-2 mb-4"><Pill label={'gamma'}/></li>*/}
            {/*        <li className="ml-2 mr-2 mb-4"><Pill label={'beta'}/></li>*/}
            {/*        <li className="ml-2 mr-2 mb-4"><Pill label={'alpha'}/></li>*/}
            {/*        <li className="ml-2 mr-2 mb-4"><Pill label={'sass'}/></li>*/}
            {/*        <li className="ml-2 mr-2 mb-4"><Pill label={'starterkit'}/></li>*/}
            {/*        <li className="ml-2 mr-2 mb-4"><Pill label={'sass'}/></li>*/}
            {/*        <li className="ml-2 mr-2 mb-4"><Pill label={'nextjs'}/></li>*/}
            {/*        <li className="ml-2 mr-2 mb-4"><Pill label={'tailwind'}/></li>*/}
            {/*        <li className="ml-2 mr-2 mb-4"><Pill label={'boilerplate'}/></li>*/}
            {/*        <li className="ml-2 mr-2 mb-4"><Pill label={'typescript'}/></li>*/}
            {/*        <li className="ml-2 mr-2 mb-4"><Pill label={'nodejs'}/></li>*/}
            {/*        <li className="ml-2 mr-2 mb-4"><Pill label={'nodejs'}/></li>*/}
            {/*        <li className="ml-2 mr-2 mb-4"><Pill label={'nodejs'}/></li>*/}
            {/*        <li className="ml-2 mr-2 mb-4"><Pill label={'nodejs'}/></li>*/}
            {/*        <li className="ml-2 mr-2 mb-4"><Pill label={'nodejs-is-the-best-thing-to-happen'}/></li>*/}
            {/*        <li className="ml-2 mr-2 mb-4"><Pill label={'hello world what is your name'}/></li>*/}
            {/*      </ul>*/}
            {/*    </div>*/}
            {/*  </div>*/}
            {/*</div>*/}

            {/*<div className="bg-vc-border-gradient rounded-lg p-px shadow-lg shadow-black/20">*/}
            {/*  <div className="rounded-lg bg-black p-3.5 lg:p-6">*/}
            {/*    <div className="grid grid-cols-1 grid-rows-1 md:grid-cols-2 lg:grid-cols-2 gap-1">*/}
            {/*      <div className="flex items-center">*/}
            {/*        <img className="w-10 h-10 rounded-full"*/}
            {/*             src="https://placehold.co/200x200"*/}
            {/*             alt="Code owner"/>*/}
            {/*        <span className="pl-2">JSLancerTeam</span>*/}
            {/*      </div>*/}
            {/*      <div className="flex justify-end">*/}
            {/*        <FHForkButton forkCount={0}/>*/}
            {/*      </div>*/}
            {/*    </div>*/}

            {/*    <div className="space-y-6 mt-6">*/}
            {/*      <div className="grid grid-cols-1 gap-5 lg:grid-cols-1 mt-4">*/}
            {/*        {items.map((item) => {*/}
            {/*          return (*/}
            {/*              <Link*/}
            {/*                  href={`/snippets/${item.slug}`}*/}
            {/*                  key={item.name}*/}
            {/*                  className="group block space-y-1.5 rounded-lg bg-gray-900 px-5 py-3 hover:bg-gray-800"*/}
            {/*              >*/}
            {/*                <div className="font-medium text-gray-200 group-hover:text-gray-50">*/}
            {/*                  {item.name}*/}
            {/*                </div>*/}

            {/*                {item.description ? (*/}
            {/*                    <div className="line-clamp-3 text-sm text-gray-400 group-hover:text-gray-300">*/}
            {/*                      {item.description}*/}
            {/*                    </div>*/}
            {/*                ) : null}*/}
            {/*              </Link>*/}
            {/*          );*/}
            {/*        })}*/}
            {/*      </div>*/}
            {/*      <ul className="flex flex-wrap">*/}
            {/*        <li className="ml-2 mr-2 mb-4"><Pill label={'sass'}/></li>*/}
            {/*        <li className="ml-2 mr-2 mb-4"><Pill label={'loremipsum'}/></li>*/}
            {/*        <li className="ml-2 mr-2 mb-4"><Pill label={'microsoft'}/></li>*/}
            {/*        <li className="ml-2 mr-2 mb-4"><Pill label={'gamma'}/></li>*/}
            {/*        <li className="ml-2 mr-2 mb-4"><Pill label={'beta'}/></li>*/}
            {/*        <li className="ml-2 mr-2 mb-4"><Pill label={'alpha'}/></li>*/}
            {/*        <li className="ml-2 mr-2 mb-4"><Pill label={'sass'}/></li>*/}
            {/*        <li className="ml-2 mr-2 mb-4"><Pill label={'starterkit'}/></li>*/}
            {/*        <li className="ml-2 mr-2 mb-4"><Pill label={'sass'}/></li>*/}
            {/*        <li className="ml-2 mr-2 mb-4"><Pill label={'nextjs'}/></li>*/}
            {/*        <li className="ml-2 mr-2 mb-4"><Pill label={'tailwind'}/></li>*/}
            {/*        <li className="ml-2 mr-2 mb-4"><Pill label={'boilerplate'}/></li>*/}
            {/*        <li className="ml-2 mr-2 mb-4"><Pill label={'typescript'}/></li>*/}
            {/*        <li className="ml-2 mr-2 mb-4"><Pill label={'nodejs'}/></li>*/}
            {/*        <li className="ml-2 mr-2 mb-4"><Pill label={'nodejs'}/></li>*/}
            {/*        <li className="ml-2 mr-2 mb-4"><Pill label={'nodejs'}/></li>*/}
            {/*        <li className="ml-2 mr-2 mb-4"><Pill label={'nodejs'}/></li>*/}
            {/*        <li className="ml-2 mr-2 mb-4"><Pill label={'nodejs-is-the-best-thing-to-happen'}/></li>*/}
            {/*        <li className="ml-2 mr-2 mb-4"><Pill label={'hello world what is your name'}/></li>*/}
            {/*      </ul>*/}
            {/*    </div>*/}
            {/*  </div>*/}
            {/*</div>*/}

            {/*<div className="bg-vc-border-gradient rounded-lg p-px shadow-lg shadow-black/20">*/}
            {/*  <div className="rounded-lg bg-black p-3.5 lg:p-6">*/}
            {/*    <div className="grid grid-cols-1 grid-rows-1 md:grid-cols-2 lg:grid-cols-2 gap-1">*/}
            {/*      <div className="flex items-center">*/}
            {/*        <img className="w-10 h-10 rounded-full"*/}
            {/*             src="https://placehold.co/200x200"*/}
            {/*             alt="Code owner"/>*/}
            {/*        <span className="pl-2">JSLancerTeam</span>*/}
            {/*      </div>*/}
            {/*      <div className="flex justify-end">*/}
            {/*        <FHForkButton forkCount={0}/>*/}
            {/*      </div>*/}
            {/*    </div>*/}

            {/*    <div className="space-y-6 mt-6">*/}
            {/*      <div className="grid grid-cols-1 gap-5 lg:grid-cols-1 mt-4">*/}
            {/*        {items.map((item) => {*/}
            {/*          return (*/}
            {/*              <Link*/}
            {/*                  href={`/snippets/${item.slug}`}*/}
            {/*                  key={item.name}*/}
            {/*                  className="group block space-y-1.5 rounded-lg bg-gray-900 px-5 py-3 hover:bg-gray-800"*/}
            {/*              >*/}
            {/*                <div className="font-medium text-gray-200 group-hover:text-gray-50">*/}
            {/*                  {item.name}*/}
            {/*                </div>*/}

            {/*                {item.description ? (*/}
            {/*                    <div className="line-clamp-3 text-sm text-gray-400 group-hover:text-gray-300">*/}
            {/*                      {item.description}*/}
            {/*                    </div>*/}
            {/*                ) : null}*/}
            {/*              </Link>*/}
            {/*          );*/}
            {/*        })}*/}
            {/*      </div>*/}
            {/*      <ul className="flex flex-wrap">*/}
            {/*        <li className="ml-2 mr-2 mb-4"><Pill label={'sass'}/></li>*/}
            {/*        <li className="ml-2 mr-2 mb-4"><Pill label={'loremipsum'}/></li>*/}
            {/*        <li className="ml-2 mr-2 mb-4"><Pill label={'microsoft'}/></li>*/}
            {/*        <li className="ml-2 mr-2 mb-4"><Pill label={'gamma'}/></li>*/}
            {/*        <li className="ml-2 mr-2 mb-4"><Pill label={'beta'}/></li>*/}
            {/*        <li className="ml-2 mr-2 mb-4"><Pill label={'alpha'}/></li>*/}
            {/*        <li className="ml-2 mr-2 mb-4"><Pill label={'sass'}/></li>*/}
            {/*        <li className="ml-2 mr-2 mb-4"><Pill label={'starterkit'}/></li>*/}
            {/*        <li className="ml-2 mr-2 mb-4"><Pill label={'sass'}/></li>*/}
            {/*        <li className="ml-2 mr-2 mb-4"><Pill label={'nextjs'}/></li>*/}
            {/*        <li className="ml-2 mr-2 mb-4"><Pill label={'tailwind'}/></li>*/}
            {/*        <li className="ml-2 mr-2 mb-4"><Pill label={'boilerplate'}/></li>*/}
            {/*        <li className="ml-2 mr-2 mb-4"><Pill label={'typescript'}/></li>*/}
            {/*        <li className="ml-2 mr-2 mb-4"><Pill label={'nodejs'}/></li>*/}
            {/*        <li className="ml-2 mr-2 mb-4"><Pill label={'nodejs'}/></li>*/}
            {/*        <li className="ml-2 mr-2 mb-4"><Pill label={'nodejs'}/></li>*/}
            {/*        <li className="ml-2 mr-2 mb-4"><Pill label={'nodejs'}/></li>*/}
            {/*        <li className="ml-2 mr-2 mb-4"><Pill label={'nodejs-is-the-best-thing-to-happen'}/></li>*/}
            {/*        <li className="ml-2 mr-2 mb-4"><Pill label={'hello world what is your name'}/></li>*/}
            {/*      </ul>*/}
            {/*    </div>*/}
            {/*  </div>*/}
            {/*</div>*/}

            <div className="flex justify-center">
              <Pagination prev={1} next={2}/>
            </div>
          </div>



        </div>
  );
}
