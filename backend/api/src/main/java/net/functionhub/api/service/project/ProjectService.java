package net.functionhub.api.service.project;

import net.functionhub.api.Code;
import net.functionhub.api.CodeUpdateResult;
import net.functionhub.api.FHFunction;
import net.functionhub.api.FHFunctions;
import net.functionhub.api.ForkRequest;
import net.functionhub.api.PageableRequest;
import net.functionhub.api.PageableResponse;
import net.functionhub.api.ProjectCreateRequest;
import net.functionhub.api.ProjectUpdateRequest;
import net.functionhub.api.Projects;

/**
 * @author Biz Melesse created on 8/17/23
 */
public interface ProjectService {

  CodeUpdateResult createFunction(String projectId);
  Projects createProject(ProjectCreateRequest projectCreateRequest);
  FHFunctions deleteFunction(String functionSlug);
  Projects deleteProject(String projectId);
  FHFunctions getAllFunctions(String projectId);
  FHFunctions updateFunction(FHFunction fhFunction);
  Projects getAllProjects();
  Projects updateProject(ProjectUpdateRequest projectUpdateRequest);
  CodeUpdateResult forkCode(ForkRequest forkRequest);
  PageableResponse getAllPublicFunctions(PageableRequest pageableRequest);

}
