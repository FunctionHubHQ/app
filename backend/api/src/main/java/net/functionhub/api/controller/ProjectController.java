package net.functionhub.api.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.functionhub.api.CodeUpdateResult;
import net.functionhub.api.Functions;
import net.functionhub.api.Project;
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
  public ResponseEntity<CodeUpdateResult> createFunction(String projectId) {
    return ResponseEntity.ok(projectService.createFunction(projectId));
  }

  @Override
  public ResponseEntity<Projects> createProject(ProjectCreateRequest projectCreateRequest) {
    return ResponseEntity.ok(projectService.createProject(projectCreateRequest));
  }

  @Override
  public ResponseEntity<Functions> deleteFunction(String body) {
    return ResponseEntity.ok(projectService.deleteFunction(body));
  }

  @Override
  public ResponseEntity<Projects> deleteProject(String body) {
    return ResponseEntity.ok(projectService.deleteProject(body));
  }

  @Override
  public ResponseEntity<Functions> getAllFunctions(String projectId) {
    return ResponseEntity.ok(projectService.getAllFunctions(projectId));
  }

  @Override
  public ResponseEntity<Projects> getAllProjects() {
    return ResponseEntity.ok(projectService.getAllProjects());
  }

  @Override
  public ResponseEntity<Project> updateProject(ProjectUpdateRequest projectUpdateRequest) {
    return ResponseEntity.ok(projectService.updateProject(projectUpdateRequest));
  }
}
