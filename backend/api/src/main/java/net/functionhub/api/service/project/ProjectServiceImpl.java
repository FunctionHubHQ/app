package net.functionhub.api.service.project;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import net.functionhub.api.data.postgres.entity.CodeCellEntity;
import net.functionhub.api.data.postgres.entity.ProjectEntity;
import net.functionhub.api.data.postgres.entity.ProjectItemEntity;
import net.functionhub.api.data.postgres.projection.FHFunctionProjection;
import net.functionhub.api.data.postgres.repo.CodeCellRepo;
import net.functionhub.api.data.postgres.repo.ProjectItemRepo;
import net.functionhub.api.data.postgres.repo.ProjectRepo;
import net.functionhub.api.service.mapper.ProjectMapper;
import net.functionhub.api.service.runtime.RuntimeService;
import net.functionhub.api.service.utils.FHUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
    entity.setProjectName(FHUtils.truncate(projectCreateRequest.getName()));
    entity.setDescription(FHUtils.truncate(projectCreateRequest.getDescription()));
    entity.setUserId(FHUtils.getSessionUser().getUid());
    entity.setUid(FHUtils.generateEntityId("p"));
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
        projectId = itemEntity.getProjectId();
        projectItemRepo.delete(itemEntity);
        codeCellRepo.delete(cellEntity);
      }
    }
    return getAllFunctions(projectId);
  }

  @Override
  public Projects deleteProject(String projectId) {
    codeCellRepo.deleteAll(
        codeCellRepo.findByProjectId(projectId));

    projectItemRepo.deleteAll(
        projectItemRepo.findByProjectIdOrderByCreatedAtDesc(projectId)
    );

    projectRepo.deleteById(projectId);

    return getAllProjects();
  }

  @Override
  public FHFunctions getAllFunctions(String projectId) {
    assert !ObjectUtils.isEmpty(projectId);
    return new FHFunctions()
        .projectId(projectId)
        .functions(
        projectMapper.mapFromCodeCellEntities(
            codeCellRepo.findByProjectId(projectId)));
  }

  @Override
  public FHFunctions updateFunction(FHFunction fhFunction) {
    if (!ObjectUtils.isEmpty(fhFunction.getProjectId())) {
      CodeCellEntity codeCell = codeCellRepo.findByUid(fhFunction.getCodeId());
      if (codeCell != null) {
        // only tags can be updated through this route for now
        if (ObjectUtils.isEmpty(fhFunction.getTags())) {
          codeCell.setTags("");
        } else {
          codeCell.setTags(fhFunction.getTags());
        }
        codeCell.setUpdatedAt(LocalDateTime.now());
        codeCellRepo.save(codeCell);
      }
      return getAllFunctions(fhFunction.getProjectId());
    }
    return new FHFunctions();
  }

  @Override
  public Projects getAllProjects() {
    List<ProjectEntity> projects = projectRepo.findByUserIdOrderByUpdatedAtDesc(FHUtils.getSessionUser().getUid());
    return new Projects().projects(projectMapper.mapFromProjectEntities(projects));
  }

  @Override
  public Projects updateProject(ProjectUpdateRequest projectUpdateRequest) {
    Optional<ProjectEntity> entityOpt = projectRepo.findById(projectUpdateRequest.getProjectId());
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
    CodeCellEntity codeCell = codeCellRepo.findByUid(forkRequest.getParentCodeId());
    if (codeCell != null) {
      codeCell.setForkCount(codeCell.getForkCount() + 1);
      codeCellRepo.save(codeCell);
      return runtimeService.updateCode(new Code()
          .code(codeCell.getCode())
          .parentId(codeCell.getUid()), true, forkRequest.getProjectId());
    }
    return new CodeUpdateResult();
  }

  @Override
  public PageableResponse getAllPublicFunctions(PageableRequest pageableRequest) {
    int page = pageableRequest.getPageNum();
    int limit = pageableRequest.getLimit();
    Sort sort = buildSort(pageableRequest);
    Pageable pageable = PageRequest.of(page, limit, sort);
    Page<FHFunctionProjection> pageableResult;
    List<FHFunction> functions;
    if (!ObjectUtils.isEmpty(pageableRequest.getQuery())) {
      pageableResult = codeCellRepo.searchAllFunctions(pageableRequest.getQuery(), PageRequest.of(page, limit));
    } else {
      pageableResult = codeCellRepo.findAllPublicFunctions(pageable);
    }
    functions = projectMapper.mapFromProjections(pageableResult.getContent(), FHUtils.getSessionUser().getUid());
    int totalPages = pageableResult.getTotalPages();
    long totalElements = pageableResult.getTotalElements();
    return new PageableResponse()
        .numPages(totalPages)
        .totalRecords(totalElements)
        .records(functions);
  }

  private Sort buildSort(PageableRequest pageableRequest) {
    Sort sort = Sort.by("created_at").descending(); // Use createdAt to prevent unstable sorting
    if (!ObjectUtils.isEmpty(pageableRequest.getSortBy()) &&
        !ObjectUtils.isEmpty(pageableRequest.getSortDir())) {
      // We only support single-column sorting for now
      String column = pageableRequest.getSortBy().get(0);
      String direction = pageableRequest.getSortDir().get(0);
      if (direction.equals("asc")) {
        sort = Sort.by(column).ascending();
      } else {
        sort = Sort.by(column).descending();
      }
    }
    return sort;
  }

  private CodeUpdateResult upsertCode(Code code, String projectId) {
    return runtimeService.updateCode(code, false, projectId);
  }
}
