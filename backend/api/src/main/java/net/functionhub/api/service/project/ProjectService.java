package net.functionhub.api.service.project;

import net.functionhub.api.Code;
import net.functionhub.api.Functions;
import net.functionhub.api.GenericResponse;
import net.functionhub.api.ProjectCreateRequest;
import net.functionhub.api.ProjectUpdateRequest;
import net.functionhub.api.Projects;

/**
 * @author Biz Melesse created on 8/17/23
 */
public interface ProjectService {

  Code createFunction();
  GenericResponse createProject(ProjectCreateRequest projectCreateRequest);
  GenericResponse deleteFunction(String functionSlug);
  GenericResponse deleteProject(String projectId);
  Functions getAllFunctions();
  Projects getAllProjects();
  GenericResponse updateProject(ProjectUpdateRequest projectUpdateRequest);

}
