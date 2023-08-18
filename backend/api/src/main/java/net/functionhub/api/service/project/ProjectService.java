package net.functionhub.api.service.project;

import net.functionhub.api.CodeUpdateResult;
import net.functionhub.api.Functions;
import net.functionhub.api.Project;
import net.functionhub.api.ProjectCreateRequest;
import net.functionhub.api.ProjectUpdateRequest;
import net.functionhub.api.Projects;

/**
 * @author Biz Melesse created on 8/17/23
 */
public interface ProjectService {

  CodeUpdateResult createFunction(String projectId);
  Projects createProject(ProjectCreateRequest projectCreateRequest);
  Functions deleteFunction(String functionSlug);
  Projects deleteProject(String projectId);
  Functions getAllFunctions(String projectId);
  Projects getAllProjects();
  Project updateProject(ProjectUpdateRequest projectUpdateRequest);

}
