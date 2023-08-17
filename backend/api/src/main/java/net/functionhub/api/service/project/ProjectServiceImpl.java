package net.functionhub.api.service.project;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.functionhub.api.Code;
import net.functionhub.api.Functions;
import net.functionhub.api.GenericResponse;
import net.functionhub.api.ProjectCreateRequest;
import net.functionhub.api.ProjectUpdateRequest;
import net.functionhub.api.Projects;
import org.springframework.stereotype.Service;

/**
 * @author Biz Melesse created on 8/17/23
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

  @Override
  public Code createFunction() {
    return null;
  }

  @Override
  public GenericResponse createProject(ProjectCreateRequest projectCreateRequest) {
    return null;
  }

  @Override
  public GenericResponse deleteFunction(String functionSlug) {
    return null;
  }

  @Override
  public GenericResponse deleteProject(String projectId) {
    return null;
  }

  @Override
  public Functions getAllFunctions() {
    return null;
  }

  @Override
  public Projects getAllProjects() {
    return null;
  }

  @Override
  public GenericResponse updateProject(ProjectUpdateRequest projectUpdateRequest) {
    return null;
  }
}
