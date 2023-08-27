import * as React from "react";
import {FC} from "react";
import FHForkButton from "#/ui/project/fh-fork-button";
import Link from "next/link";
import Pill from "#/ui/pill";
import Pagination from "#/ui/pagination";
import {PageableResponse} from "#/codegen";
import AddProjectButton from "#/ui/project/add-project-button";
import {Boundary} from "#/ui/boundary";
import {AddressBar} from "#/ui/address-bar";

export interface ExploreViewProps {
  data: PageableResponse,
  prev: number,
  next: number
}

const ExploreView: FC<ExploreViewProps> = (props) => {
  const getTags = (tags: string | undefined): Array<string> => {
    if (tags && tags.length > 0) {
      return tags.split(",")
      .map(t => t.trim())
      .filter(t => t)
    }
    return new Array<string>()
  }

  return (
      <div className="min-h-screen pt-20">
        <div className="mx-auto max-w-4xl space-y-8 px-2 pt-20 lg:px-8 lg:py-8">
          {/*<div className="rounded-lg bg-black">*/}
          {/*  <AddressBar />*/}
          {/*</div>*/}
          {props.data?.records?.length ?
              props.data?.records?.map(record => {
                return (
                    <div className="bg-vc-border-gradient rounded-lg p-px shadow-lg shadow-black/20" key={record.code_id}>
                      <div className="rounded-lg bg-black p-3.5 lg:p-6">
                        <div className="grid grid-cols-1 grid-rows-1 md:grid-cols-2 lg:grid-cols-2 gap-1">
                          <div className="flex items-center">
                            <img className="w-10 h-10 rounded-full"
                                 src={record.owner_avatar}
                                 alt="Code owner"/>
                            <span className="pl-2">{record.owner_username}</span>
                          </div>
                          <div className="flex justify-end">
                            <FHForkButton forkCount={record.fork_count}/>
                          </div>
                        </div>

                        <div className="space-y-6 mt-6">
                          <div className="grid grid-cols-1 gap-5 lg:grid-cols-1 mt-4">
                            <Link
                                href={`/explore/view/${record.slug}`}
                                className="group block space-y-1.5 rounded-lg bg-gray-900 px-5 py-3 hover:bg-gray-800"
                            >
                              <div className="font-medium text-gray-200 group-hover:text-gray-50">
                                {record.name}
                              </div>

                              {record.description ? (
                                  <div className="line-clamp-3 text-sm text-gray-400 group-hover:text-gray-300">
                                    {record.description}
                                  </div>
                              ) : null}
                            </Link>
                          </div>
                          <ul className="flex flex-wrap">
                            {getTags(record.tags).map((tag, index) => {
                              return (
                                  <li className="ml-2 mr-2 mb-4" key={"tag-" + index}><Pill label={tag}/></li>
                              )
                            })}
                          </ul>
                        </div>
                      </div>
                    </div>
                )
              }) :

              <div className="rounded-lg bg-black p-3.5 lg:p-6">
              <Boundary labels={['explore']} color="pink" animateRerendering={false}>
                <div className="text-vercel-pink space-y-4">
                  <h2 className="text-lg text-vercel-pink font-bold">No more pages</h2>
                  <p className="text-sm">You have reached the end :(</p>
                </div>
              </Boundary>

              </div>
          }

          <div className="flex justify-center">
            <Pagination prev={props.prev} next={props.next} currentPageRecordCount={props.data?.records?.length}/>
          </div>
        </div>
      </div>
  );
};

export default ExploreView;
