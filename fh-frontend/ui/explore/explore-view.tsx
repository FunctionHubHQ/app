'use client'
import * as React from "react";
import {FC, useState} from "react";
import FHForkButton from "#/ui/project/fh-fork-button";
import Link from "next/link";
import Pill from "#/ui/pill";
import Pagination from "#/ui/pagination";
import {CodeUpdateResult, PageableResponse, ProjectApi, UserProfile} from "#/codegen";
import {Boundary} from "#/ui/boundary";
import {getAuthToken, headerConfig} from "#/ui/utils/headerConfig";
import {useAuthContext} from "#/context/AuthContext";
import {useRouter} from "next/navigation";
import {ERROR} from "#/ui/utils/utils";
import ProjectSelectionModal from "#/ui/explore/project-selection-modal";

export interface ExploreViewProps {
  data: PageableResponse,
  prev: number,
  next: number
}

const ExploreView: FC<ExploreViewProps> = (props) => {
  const { authUser, fhUser } = useAuthContext()
  const router = useRouter()
  const[openForkModal, setOpenForkModal] = useState(false)

  const getTags = (tags: string | undefined): Array<string> => {
    if (tags && tags.length > 0) {
      return tags.split(",")
      .map(t => t.trim())
      .filter(t => t)
    }
    return new Array<string>()
  }

  const signIn = () => {
    router.push("/login")
  }

  const doFork = async (codeId: string, projectId?: string) => {
    // TODO: show a loader here
    setOpenForkModal(false)
    const token = await getAuthToken(authUser)
    if (token) {
      new ProjectApi(headerConfig(token)).forkCode({
        parent_code_id: codeId,
        project_id: projectId
      }).then(response => {
        const updateResult: CodeUpdateResult = response.data
        console.log("FORK RESULT: ", updateResult)
        router.push(`/hub/projects/${updateResult.project_id}/edit:${updateResult.code_id}`)
      }).catch(e => ERROR(e))
    }
  }

  const maybeFork = async (codeId: string) => {
    const userProfile: UserProfile = fhUser as UserProfile
    if (userProfile.projects && userProfile.projects.length > 0) {
      // If a user has any number of projects, show the modal so they can choose
      // the fork target
      setOpenForkModal(true)
    } else {
      await doFork(codeId, undefined)
    }
  }

  return (
      <div className="min-h-screen pt-4">
        <div className="mx-auto md:max-w-4xl space-y-8 px-2 pt-20 lg:px-8 lg:py-8 max-w-full">
          {props.data?.records?.length ?
              props.data?.records?.map(record => {
                return (
                    <div className="bg-vc-border-gradient rounded-lg p-px shadow-lg shadow-black/20" key={record.code_id}>
                      <div className="rounded-lg bg-black p-3.5 lg:p-6">
                        <ProjectSelectionModal
                            projects={fhUser?.projects}
                            isOpen={openForkModal}
                            onDoFork={doFork}
                            onClose={() => setOpenForkModal(false)}
                            codeId={record.code_id as string}/>
                        <div className="grid grid-cols-1 grid-rows-1 md:grid-cols-2 lg:grid-cols-2 gap-1">
                          <div className="flex items-center">
                            <img className="w-10 h-10 rounded-full"
                                 src={record.owner_avatar}
                                 alt="Code owner"/>
                            <span className="pl-2">{record.owner_username}</span>
                          </div>
                          {!record?.is_owner &&
                          <div className="flex justify-end">
                            <FHForkButton
                                label={authUser ? 'Fork' : 'Sign in to Fork'}
                                showCount={true}
                                forkCount={record.fork_count}
                                onClick={authUser ? () => maybeFork(record.code_id as string) : () => signIn()}
                            />
                          </div>
                          }
                        </div>

                        <div className="space-y-6 mt-6">
                          <div className="grid grid-cols-1 gap-5 lg:grid-cols-1 mt-4">
                            <Link
                                href={`/view/${record.slug}`}
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
                  <h2 className="text-lg text-vercel-pink font-bold">There is nothing here</h2>
                  <p className="text-sm">{"Sorry, we couldn't find pages to show :("}</p>
                </div>
              </Boundary>
              </div>
          }

          <div className="flex justify-center">
            {
              !!props.data?.records?.length  && <Pagination prev={props.prev} next={props.next} currentPageRecordCount={props.data?.records?.length}/>
            }
          </div>
        </div>
      </div>
  );
};

export default ExploreView;
