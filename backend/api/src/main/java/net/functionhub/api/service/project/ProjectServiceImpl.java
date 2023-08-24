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
import net.functionhub.api.FHFunction;
import net.functionhub.api.FHFunctions;
import net.functionhub.api.ForkRequest;
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
    return upsertCode(new Code()
        .isActive(true)
        .isPublic(false)
        .code(Base64.getEncoder().encodeToString(template.getBytes())), projectId);

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
  public FHFunctions deleteFunction(String functionSlug) {
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
  public FHFunctions getAllFunctions(String projectId) {
    return new FHFunctions()
        .projectId(projectId)
        .functions(
        projectMapper.mapFromCodeCellEntities(
            codeCellRepo.findByProjectId(UUID.fromString(projectId))));
  }

  @Override
  public FHFunctions updateFunction(FHFunction fhFunction) {
    if (!ObjectUtils.isEmpty(fhFunction.getProjectId())) {
      CodeCellEntity codeCell = codeCellRepo.findByUid(UUID.fromString(fhFunction.getCodeId()));
      if (codeCell != null) {
        // only tags can be updated through this route for now
        codeCell.setTags(fhFunction.getTags());
        codeCellRepo.save(codeCell);
      }
      return getAllFunctions(fhFunction.getProjectId());
    }
    return getAllFunctions(null);
  }

  @Override
  public Projects getAllProjects() {
    List<ProjectEntity> projects = projectRepo.findByUserIdOrderByUpdatedAtDesc(FHUtils.getSessionUser().getUid());
    return new Projects().projects(projectMapper.mapFromProjectEntities(projects));
  }

  @Override
  public Projects updateProject(ProjectUpdateRequest projectUpdateRequest) {
    Optional<ProjectEntity> entityOpt = projectRepo.findById(UUID.fromString(projectUpdateRequest.getProjectId()));
    if (entityOpt.isPresent()) {
      ProjectEntity entity = entityOpt.get();
      entity.setProjectName(projectUpdateRequest.getName());
      entity.setDescription(projectUpdateRequest.getDescription());
      entity.setUpdatedAt(LocalDateTime.now());
      projectRepo.save(entity);
    }
    return getAllProjects();
  }

  @Override
  public CodeUpdateResult forkCode(ForkRequest forkRequest) {
    CodeCellEntity codeCell = codeCellRepo.findByUid(UUID.fromString(forkRequest.getParentCodeId()));
    if (codeCell != null) {
      codeCell.setForkCount(codeCell.getForkCount() + 1);
      codeCellRepo.save(codeCell);
      return runtimeService.updateCode(new Code()
          .code(codeCell.getCode())
          .parentId(codeCell.getUid().toString()));
    }
    return new CodeUpdateResult();
  }

  private CodeUpdateResult upsertCode(Code code, String projectId) {
    CodeUpdateResult result = runtimeService.updateCode(code);
    ProjectItemEntity projectItemEntity = new ProjectItemEntity();
    projectItemEntity.setProjectId(UUID.fromString(projectId));
    projectItemEntity.setUid(UUID.randomUUID());
    projectItemEntity.setCodeId(UUID.fromString(result.getUid()));
    projectItemRepo.save(projectItemEntity);
    return result;
  }
}
