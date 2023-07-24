//package com.gptlambda.api.service.project;
//
//import com.gptlambda.api.data.postgres.entity.MediaContentEntity;
//import com.gptlambda.api.data.postgres.entity.ProjectEntity;
//import com.gptlambda.api.data.postgres.entity.SampledImageEntity;
//import com.gptlambda.api.data.postgres.projection.UserProjectProjection;
//import com.gptlambda.api.data.postgres.repo.MediaContentRepo;
//import com.gptlambda.api.data.postgres.repo.ProjectRepo;
//import com.gptlambda.api.data.postgres.repo.SampledImageRepo;
//import com.gptlambda.api.data.postgres.repo.UserRepo;
//import com.gptlambda.api.props.AWSProps;
//import com.gptlambda.api.props.UserProps;
//import com.gptlambda.api.service.av.AVService;
//import com.gptlambda.api.service.gif.GifService;
//import com.gptlambda.api.service.mapper.ProjectMapper;
//import com.gptlambda.api.service.user.UserService;
//import com.gptlambda.api.service.utils.ShoppiemUtils;
//import com.gptlambda.api.utils.security.firebase.SecurityFilter;
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.gptlambda.api.ContentPreviewResponse;
//import com.gptlambda.api.CreateProjectRequest;
//import com.gptlambda.api.CreateProjectResponse;
//import com.gptlambda.api.GenericResponse;
//import com.gptlambda.api.GifRequest;
//import com.gptlambda.api.GifResponse;
//import com.gptlambda.api.JobStatus;
//import com.gptlambda.api.KalicoContentType;
//import com.gptlambda.api.MediaContent;
//import com.gptlambda.api.PageableResponse;
//import com.gptlambda.api.ProjectDetail;
//import com.gptlambda.api.ProjectJobStatus;
//import com.gptlambda.api.UpdateProjectContentRequest;
//import com.gptlambda.api.UserProjectsResponse;
//import java.net.URI;
//import java.net.URISyntaxException;
//import java.time.LocalDateTime;
//import java.time.ZoneOffset;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Optional;
//import java.util.stream.Collectors;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.domain.Sort;
//import org.springframework.stereotype.Service;
//import org.springframework.util.ObjectUtils;
//
///**
// * @author Biz Melesse created on 1/29/23
// */
//@Slf4j
//@Service
//@RequiredArgsConstructor
//public class ProjectServiceImpl implements ProjectService {
//  private final MediaContentRepo mediaContentRepo;
//  private final SampledImageRepo sampledImageRepo;
//  private final ProjectRepo projectRepo;
//  private final AWSProps awsProps;
//  private final SecurityFilter securityFilter;
//  private final ProjectMapper projectMapper;
//  private final ObjectMapper objectMapper;
//  private final UserService userService;
//  private final UserProps userProps;
//  private final UserRepo userRepo;
//
//  @Override
//  public CreateProjectResponse createProject(CreateProjectRequest createProjectRequest) {
////    userService.createUser();
////    if (createProjectRequest != null) {
////      // If a file is uploaded, use that. Otherwise, use the url
////      String url = null;
////      String file = null;
////      String ext = null;
////      if (createProjectRequest.getContentLink().isPresent()) {
////        url = createProjectRequest.getContentLink().get();
////      }
////      if (createProjectRequest.getFile().isPresent()) {
////        file = createProjectRequest.getFile().get();
////      }
////      if (createProjectRequest.getFileExtension().isPresent()) {
////        ext = createProjectRequest.getFileExtension().get();
////      }
////     if (!ObjectUtils.isEmpty(file) &&  !ObjectUtils.isEmpty(ext)) {
////        url = null;
////      }  else if (!ObjectUtils.isEmpty(url)) {
////       url = avService.normalizeUrl(url);
////       if (!isSupportedUrl(url)) {
////         return new CreateProjectResponse().error("The link provided is not yet supported");
////       }
////       file = null;
////       ext = null;
////     } else {
////       throw new RuntimeException("Please provide a link or upload a supported file");
////      }
////      String userId = securityFilter.getUser().getFirebaseId();
////      ProjectEntity entity = new ProjectEntity();
////      if (!ObjectUtils.isEmpty(createProjectRequest.getProjectName().get())) {
////        entity.setProjectName(createProjectRequest.getProjectName().get());
////      }
////      // TODO: Users may try to create duplicate content from the same source link.
////      //    We are allowing this for now.
////      entity.setUserId(userId);
////      entity.setEmbedImages(createProjectRequest.getEmbedImages().get());
////      entity.setParaphrase(createProjectRequest.getParaphrase().get());
////      if (!createProjectRequest.getContentType().isPresent()) {
////        entity.setContentType(KalicoContentType.OTHER.getValue());
////      } else {
////        entity.setContentType(createProjectRequest.getContentType().get().getValue());
////      }
////      entity.setContentLink(url);
////      entity.setProjectUid(ShoppiemUtils.generateUid());
////      if (createProjectRequest.getGetRawTranscript() != null) {
////        entity.setGetRawTranscript(createProjectRequest.getGetRawTranscript());
////      }
////      projectRepo.save(entity);
////      avService.processMedia(url, entity.getId(), file, ext);
////      return new CreateProjectResponse()
////          .status("OK")
////          .projectName(entity.getProjectName())
////          .projectId(entity.getProjectUid());
////    }
//    return new CreateProjectResponse().error("Encountered an error while creating the project");
//  }
//  @Override
//  public GenericResponse deleteProject(String projectUid) {
//    // TODO: remove the corresponding media files from S3
//    if (!ObjectUtils.isEmpty(projectUid)) {
//      Optional<ProjectEntity> projectEntityOpt = projectRepo.findByProjectUid(projectUid);
//      projectEntityOpt.ifPresent(projectEntity -> projectRepo.deleteById(projectEntity.getId()));
//      return new GenericResponse().status("OK");
//    }
//    return new GenericResponse().error("Invalid project id received. Deletion failed.");
//  }
//
//  @Override
//  public PageableResponse getAllProjects() {
//    String userId = securityFilter.getUser().getFirebaseId();
//    List<ProjectEntity> projects = projectRepo.findAllProjectsByUserId(userId);
//    PageableResponse response = new PageableResponse();
//    response.setRecords(projectMapper.map(projects));
//    response.setNumPages(1);
//    response.setTotalRecords(response.getRecords().size());
//     return response;
//  }
//
//  @Override
//  public ProjectDetail getProjectById(String projectUid) {
//    Optional<ProjectEntity> entityOpt = projectRepo.findByProjectUid(projectUid);
//    return entityOpt.map(projectEntity -> projectMapper.map(projectEntity, objectMapper))
//        .orElse(null);
//  }
//
//  @Override
//  public GenericResponse updateProjectContent(
//      UpdateProjectContentRequest updateProjectContentRequest) {
//    if (updateProjectContentRequest != null &&
//        updateProjectContentRequest.getProjectUid() != null) {
//      String userId = securityFilter.getUser().getFirebaseId();
//      Optional<ProjectEntity> project = projectRepo.findProjectByUserIdAndProjectUid(userId,
//          updateProjectContentRequest.getProjectUid());
//      if (project.isPresent()) {
//        ProjectEntity entity = project.get();
//        entity.setUpdatedAt(LocalDateTime.now());
//        if (!ObjectUtils.isEmpty(updateProjectContentRequest.getContent())) {
//          try {
//            entity.setContent(objectMapper.writeValueAsString(updateProjectContentRequest.getContent()));
//          } catch (JsonProcessingException e) {
//            log.error("ProjectServiceImpl.updateProjectContent: {}", e.getMessage());
//          }
//        } else {
//          entity.setContent(null);
//        }
//        projectRepo.save(entity);
//        return new GenericResponse().status("OK");
//      }
//    }
//    return new GenericResponse().error("Project not found");
//  }
//
//  @Override
//  public GenericResponse deleteGif(String url) {
//    gifService.deleteGif(url);
//    return new GenericResponse().status("OK");
//  }
//
//  @Override
//  public GifResponse generateGif(GifRequest gifRequest) {
//    String error = "";
//    if (gifRequest.getProjectId() != null) {
//      if (gifRequest.getStart() != null && gifRequest.getEnd() != null) {
//        if (gifRequest.getStart() >= 0 && gifRequest.getEnd() >= 0) {
//          if (gifRequest.getStart() < gifRequest.getEnd()) {
//            Optional<ProjectEntity> projectEntityOpt = projectRepo.findByProjectUid(gifRequest.getProjectId());
//            MediaContentEntity entity = null;
//            if (projectEntityOpt.isPresent()) {
//              entity = mediaContentRepo.findByProjectId(projectEntityOpt.get().getId());
//            }
//            if (entity == null) {
//              error = "Could not find a record of this video in the database";
//            } else {
//              //                  .url("https://media.giphy.com/media/sbERZAClpniGFsDKhS/giphy.gif")
//              return new GifResponse()
//                  .url(gifService.generateGif(entity.getMediaId(), gifRequest.getStart(), gifRequest.getEnd()))
//                  .status("OK");
//            }
//          } else {
//            error = "Start time cannot be equal to or greater than end time";
//          }
//        } else {
//          error = "Start and end times must be equal to or greater than 0";
//        }
//      } else {
//        error = "Start or end timestamps cannot be empty";
//      }
//
//    } else {
//      error = "Document ID not provided";
//    }
//    return new GifResponse()
//        .error(error);
//  }
//
//  @Override
//  public List<String> getSampledImages(String projectUid) {
//    Optional<ProjectEntity> projectEntityOpt = projectRepo.findByProjectUid(projectUid);
//    List<SampledImageEntity> entities = new ArrayList<>();
//    if (projectEntityOpt.isPresent()) {
//      entities = sampledImageRepo.findByProjectIdOrderByImageKeyAsc(
//          projectEntityOpt.get().getId());
//    }
//    List<String> imageUrls = new ArrayList<>();
//    if (!ObjectUtils.isEmpty(entities)) {
//      return entities
//          .stream()
//          .map(it -> awsProps.getCdn() + "/" + it.getImageKey()).
//          collect(Collectors.toList());
//    }
//    return imageUrls;
//  }
//
//  @Override
//  public MediaContent getMediaContent(String projectUid) {
//    Optional<ProjectEntity> projectEntityOpt = projectRepo.findByProjectUid(projectUid);
//    return projectEntityOpt.map(projectEntity -> projectMapper.mapMediaContent(
//        mediaContentRepo.findByProjectId(projectEntity.getId()),
//        projectEntityOpt.get().getProjectUid())).orElse(null);
//  }
//
//  @Override
//  public ProjectJobStatus getProjectJobStatus(String projectUid) {
//    // Return percent complete until the processed field is set. If so, return 100%.
//    // Compute the progress by taking the creation time and the elapsed time
//    String userId = securityFilter.getUser().getFirebaseId();
//    Optional<ProjectEntity> entityOpt =  projectRepo.findPendingJob(userId);
//    if (entityOpt.isPresent() && isRecent(entityOpt.get().getCreatedAt())) {
//      ProjectEntity projectEntity = entityOpt.get();
//      long percent;
//      String message = "";
//      JobStatus status = JobStatus.IN_PROGRESS;
//      String estimate = "Less than 5 minutes";
//      if (projectEntity.getFailed() != null && projectEntity.getFailed()) {
//        status = JobStatus.FAILED;
//        message = projectEntity.getReasonFailed();
//        percent = 0;
//      } else if (projectEntity.getProcessed() != null && projectEntity.getProcessed()) {
//        percent = 100;
//        status = JobStatus.COMPLETE;
//      } else {
//        long delta = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC) - projectEntity.getCreatedAt().toEpochSecond(ZoneOffset.UTC);
//        if (delta > projectProps.getMaxJobTime()) {
//          // If we're still not done with the processing, increase the estimate by 5 minutes every five minutes
//          percent = 95;
//          long res = delta/projectProps.getMaxJobTime();
//          long estimatedTime = (res + 1) * (projectProps.getMaxJobTime()/60);
//          estimate = String.format("Less than %s minutes", estimatedTime);
//        } else {
//          percent = (int)(100 * (delta/(projectProps.getMaxJobTime()* 1.0)));
//          // Do not reach 100% unless the processing is actually complete
//          if (percent == 100) {
//            percent--;
//          }
//        }
//      }
//      return new ProjectJobStatus()
//          .projectId(projectEntity.getProjectUid())
//          .projectName(projectEntity.getProjectName())
//          .percentComplete(percent)
//          .estimatedTime(estimate)
//          .message(message)
//          .status(status);
//    }
//    return new ProjectJobStatus()
//        .status(JobStatus.PROJECT_NOT_FOUND);
//  }
//
//  private boolean isRecent(LocalDateTime createdAt) {
//    // Consider anything that is less than an hour old to be recent
//    return Math.abs(createdAt.toEpochSecond(ZoneOffset.UTC) -
//        LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)) < 60*60;
//  }
//
//  @Override
//  public ContentPreviewResponse getContentPreview(String url) {
//    return avService.downloadContentMetadata(avService.normalizeUrl(url));
//  }
//
//  @Override
//  public UserProjectsResponse getAllUserProjects(Integer pageNum, Integer limit) {
//    String email = securityFilter.getUser().getEmail();
//    if (userProps.getAdminEmails().contains(email)) {
//      long numUsers = userRepo.count();
//      Pageable pageable = PageRequest.of(pageNum, limit,
//          Sort.by("projectCreatedAt").descending());
//      Page<UserProjectProjection> page = projectRepo.findAllUserProjects(pageable);
//
//      UserProjectsResponse response = new UserProjectsResponse();
//      response.setNumUsers(numUsers);
//      response.setTotalRecords(page.getTotalElements());
//      response.setNumPages(page.getTotalPages());
//      response.setRecords(projectMapper.mapProjections(page.getContent()));
//      return response;
//    }
//    return new UserProjectsResponse()
//        .numUsers(0L)
//        .numPages(0)
//        .totalRecords(0L)
//        .records(new ArrayList<>());
//  }
//
//  private boolean isSupportedUrl(String url) {
//    if (isValidUrl(url)) {
//      for (String supportedDomain : projectProps.getSupportedDomains()) {
//        if (url.toLowerCase().contains(supportedDomain.toLowerCase())) {
//          return true;
//        }
//      }
//    }
//    return false;
//  }
//
//  private boolean isValidUrl(String url) {
//    if (!ObjectUtils.isEmpty(url)) {
//      try {
//        URI uri = new URI(url);
//        if (!ObjectUtils.isEmpty(uri.getHost())) {
//          return true;
//        }
//      } catch (URISyntaxException e) {
//        return false;
//      }
//    }
//    return false;
//  }
//}
