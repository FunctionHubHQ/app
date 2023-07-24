//package com.gptlambda.api.service.project;
//
//import com.gptlambda.api.ContentPreviewResponse;
//import com.gptlambda.api.CreateProjectRequest;
//import com.gptlambda.api.CreateProjectResponse;
//import com.gptlambda.api.GenericResponse;
//import com.gptlambda.api.GifRequest;
//import com.gptlambda.api.GifResponse;
//import com.gptlambda.api.MediaContent;
//import com.gptlambda.api.PageableResponse;
//import com.gptlambda.api.ProjectDetail;
//import com.gptlambda.api.ProjectJobStatus;
//import com.gptlambda.api.UpdateProjectContentRequest;
//import com.gptlambda.api.UserProjectsResponse;
//import java.util.List;
//
///**
// * @author Biz Melesse created on 1/29/23
// */
//public interface ProjectService {
//
//  /**
//   * Create a new project
//   *
//   * @param createProjectRequest project metadata and an optional uploaded media content
//   * @return
//   */
//  CreateProjectResponse createProject(CreateProjectRequest createProjectRequest);
//  GenericResponse deleteProject(String projectUid);
//  PageableResponse getAllProjects();
//  ProjectDetail getProjectById(String projectUid);
//
//  GenericResponse updateProjectContent(
//      UpdateProjectContentRequest updateProjectContentRequest);
//
//  GenericResponse deleteGif(String url);
//
//  GifResponse generateGif(GifRequest gifRequest);
//
//  List<String> getSampledImages(String projectUid);
//
//  MediaContent getMediaContent(String projectUid);
//
//  ProjectJobStatus getProjectJobStatus(String projectUid);
//
//  /**
//   * Get OpenGraph social preview links for the given content
//   * @param url
//   * @return
//   */
//  ContentPreviewResponse getContentPreview(String url);
//
//  /**
//   * Get all user projects for an admin
//   *
//   * @param page
//   * @param limit
//   * @return
//   */
//  UserProjectsResponse getAllUserProjects(Integer page, Integer limit);
//
//}
