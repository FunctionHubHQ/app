package net.functionhub.api.service.project;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.functionhub.api.Code;
import net.functionhub.api.CodeUpdateResult;
import net.functionhub.api.Functions;
import net.functionhub.api.Project;
import net.functionhub.api.ProjectCreateRequest;
import net.functionhub.api.ProjectUpdateRequest;
import net.functionhub.api.Projects;
import net.functionhub.api.data.postgres.entity.CodeCellEntity;
import net.functionhub.api.data.postgres.entity.ProjectEntity;
import net.functionhub.api.data.postgres.entity.ProjectItemEntity;
import net.functionhub.api.data.postgres.repo.CodeCellRepo;
import net.functionhub.api.data.postgres.repo.ProjectItemRepo;
import net.functionhub.api.data.postgres.repo.ProjectRepo;
import net.functionhub.api.service.mapper.ProjectMapper;
import net.functionhub.api.service.runtime.RuntimeService;
import net.functionhub.api.service.utils.FHUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

/**
 * @author Biz Melesse created on 8/17/23
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {
  private final ProjectRepo projectRepo;
  private final CodeCellRepo codeCellRepo;
  private final ProjectItemRepo projectItemRepo;
  private final ProjectMapper projectMapper;
  private final RuntimeService runtimeService;

  @Override
  public CodeUpdateResult createFunction(String projectId) {
    String template = FHUtils.loadFile("ts/functionTemplate.ts");
    Code code = new Code()
        .isActive(true)
        .isPublic(false)
        .userId(FHUtils.getSessionUser().getUid())
        .code(Base64.getEncoder().encodeToString(template.getBytes()));
    CodeUpdateResult result = runtimeService.updateCode(code);
    ProjectItemEntity projectItemEntity = new ProjectItemEntity();
    projectItemEntity.setProjectId(UUID.fromString(projectId));
    projectItemEntity.setUid(UUID.randomUUID());
    projectItemEntity.setCodeId(UUID.fromString(result.getUid()));
    projectItemRepo.save(projectItemEntity);
    return result;
  }

  @Override
  public Projects createProject(ProjectCreateRequest projectCreateRequest) {
    ProjectEntity entity = new ProjectEntity();
    entity.setProjectName(projectCreateRequest.getName());
    entity.setDescription(projectCreateRequest.getDescription());
    entity.setUserId(FHUtils.getSessionUser().getUid());
    entity.setUid(UUID.randomUUID());
    projectRepo.save(entity);
    return getAllProjects();
  }

  @Override
  public Functions deleteFunction(String functionSlug) {
    String projectId = "";
    if (!ObjectUtils.isEmpty(functionSlug)) {
      CodeCellEntity cellEntity = codeCellRepo.findBySlug(functionSlug);
      if (cellEntity != null) {
        ProjectItemEntity itemEntity = projectItemRepo.findByCodeId(cellEntity.getUid());
        projectId = itemEntity.getProjectId().toString();
        projectItemRepo.delete(itemEntity);
        codeCellRepo.delete(cellEntity);
      }
    }
    return getAllFunctions(projectId);
  }

  @Override
  public Projects deleteProject(String projectId) {
    codeCellRepo.deleteAll(
        codeCellRepo.findByProjectId(UUID.fromString(projectId)));

    projectItemRepo.deleteAll(
        projectItemRepo.findByProjectIdOrderByCreatedAtDesc(UUID.fromString(projectId))
    );

    projectRepo.deleteById(UUID.fromString(projectId));

    return getAllProjects();
  }

  @Override
  public Functions getAllFunctions(String projectId) {
    return new Functions().functions(
        projectMapper.mapFromCodeCellEntities(
            codeCellRepo.findByProjectId(UUID.fromString(projectId))));
  }

  @Override
  public Projects getAllProjects() {
    List<ProjectEntity> projects = projectRepo.findByUserIdOrderByCreatedAtDesc(FHUtils.getSessionUser().getUid());
    return new Projects().projects(projectMapper.mapFromProjectEntities(projects));
  }

  @Override
  public Project updateProject(ProjectUpdateRequest projectUpdateRequest) {
    Optional<ProjectEntity> entityOpt = projectRepo.findById(UUID.fromString(projectUpdateRequest.getProjectId()));
    if (entityOpt.isPresent()) {
      ProjectEntity entity = entityOpt.get();
      entity.setProjectName(projectUpdateRequest.getName());
      entity.setDescription(projectUpdateRequest.getDescription());
      entity.setUpdatedAt(LocalDateTime.now());
      projectRepo.save(entity);
      return projectMapper.mapFromProjectEntity(entity);
    }
    return null;
  }
}
