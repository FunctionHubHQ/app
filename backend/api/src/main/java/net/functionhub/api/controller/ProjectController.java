package net.functionhub.api.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.functionhub.api.Code;
import net.functionhub.api.Functions;
import net.functionhub.api.GenericResponse;
import net.functionhub.api.ProjectApi;
import net.functionhub.api.ProjectCreateRequest;
import net.functionhub.api.ProjectUpdateRequest;
import net.functionhub.api.Projects;
import net.functionhub.api.service.project.ProjectService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Biz Melesse created on 8/17/23
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class ProjectController implements ProjectApi {
  private final ProjectService projectService;

  @Override
  public ResponseEntity<Code> createFunction() {
    return ResponseEntity.ok(projectService.createFunction());
  }

  @Override
  public ResponseEntity<GenericResponse> createProject(ProjectCreateRequest projectCreateRequest) {
    return ResponseEntity.ok(projectService.createProject(projectCreateRequest));
  }

  @Override
  public ResponseEntity<GenericResponse> deleteFunction(String body) {
    return ResponseEntity.ok(projectService.deleteFunction(body));
  }

  @Override
  public ResponseEntity<GenericResponse> deleteProject(String body) {
    return ResponseEntity.ok(projectService.deleteProject(body));
  }

  @Override
  public ResponseEntity<Functions> getAllFunctions() {
    return ResponseEntity.ok(projectService.getAllFunctions());
  }

  @Override
  public ResponseEntity<Projects> getAllProjects() {
    return ResponseEntity.ok(projectService.getAllProjects());
  }

  @Override
  public ResponseEntity<GenericResponse> updateProject(ProjectUpdateRequest projectUpdateRequest) {
    return ResponseEntity.ok(projectService.updateProject(projectUpdateRequest));
  }
}
