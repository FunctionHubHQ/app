package net.functionhub.api.service.utils;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.StringJoiner;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.functionhub.api.Code;
import net.functionhub.api.FHFunction;
import net.functionhub.api.ProjectCreateRequest;
import net.functionhub.api.Projects;
import net.functionhub.api.UserProfile;
import net.functionhub.api.data.postgres.entity.UserEntity;
import net.functionhub.api.data.postgres.repo.UserRepo;
import net.functionhub.api.service.project.ProjectService;
import net.functionhub.api.service.runtime.RuntimeService;
import net.functionhub.api.utils.security.Credentials;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

/**
 * @author Biz Melesse created on 8/25/23
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SeedData {
  private final ProjectService projectService;
  private final RuntimeService runtimeService;
  private final WordList wordList;
  private final UserRepo userRepo;
  private final int numProjects = 12;
  private final int numPrivateFunctions = 12;
  private final int numPublicFunctions = 12;

  public void generateSeedData() {
    generateSeedData("bmelesse@elifsis.com");
    generateSeedData(null);
  }

  public void generateSeedData(String email) {
    int minProjectNameLength = 1;
    int maxProjectNameLength = 6;
    int minProjectDescLength = 0;
    int maxProjectDescLength = 50;
    Random rand = new Random();
    log.info("Generating seed data");
    for (int i = 0; i < numProjects; i++) {
      setSecurityContext(email);
      int nameLength = rand.nextInt(maxProjectNameLength - minProjectNameLength + 1) + minProjectNameLength;
      int descLength = rand.nextInt(maxProjectDescLength - minProjectDescLength + 1) + minProjectDescLength;
      ProjectCreateRequest request1 = new ProjectCreateRequest()
          .name("Project: " + wordList.getRandomPhrase(nameLength, false))
          .description("Description: " + wordList.getRandomPhrase(descLength, false));
      Projects projects = projectService.createProject(request1);
      String projectId = projects.getProjects().get(0).getProjectId();
      for (int j = 0; j < numPublicFunctions + numPrivateFunctions; j++) {
        projectService.createFunction(projectId);
      }

      // Toggle all these functions to public
      int publicCount = 0;
      for (FHFunction function : projectService.getAllFunctions(projectId).getFunctions()) {

        if (publicCount < numPublicFunctions) {
          Code code = new Code().isPublic(true).uid(function.getCodeId())
              .fieldsToUpdate(List.of("is_public"));
          runtimeService.updateCode(code);
          publicCount++;
        }

        projectService.updateFunction(
            new FHFunction().tags(generateTags())
                .projectId(projectId)
                .codeId(function.getCodeId()));
      }

    }
    log.info("Finished generated seed data");
  }

  private void setSecurityContext(String email) {
    UserEntity userEntity = new UserEntity();
    if (!ObjectUtils.isEmpty(email)) {
      userEntity = userRepo.findByEmail(email);
    } else {
      userEntity.setEmail(wordList.getRandomPhrase(3, true) + "@gmail.com");
      userEntity.setUid("u_" + FHUtils.generateUid(FHUtils.SHORT_UID_LENGTH));
      userEntity.setUsername(wordList.getRandomPhrase(3, true)
          .replace("-", "_"));
      userEntity.setAvatarUrl("https://i.pravatar.cc/300?uniquifier=" + UUID.randomUUID());
      userEntity.setFullName("Bob Lee");
      userRepo.save(userEntity);
    }
    if (userEntity != null) {
      UserProfile userProfile = new UserProfile().uid(userEntity.getUid());
      UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
          userProfile, new Credentials(),
          new HashSet<>());
      SecurityContextHolder.getContext().setAuthentication(authentication);
    } else {
      log.error("SeedData failed to create a user");
    }
  }

  private String generateTags() {
    StringJoiner joiner = new StringJoiner(",");
    Random rand = new Random();
    int minTagLength = 0;
    int maxTagLength = 20;
    int tagLength = rand.nextInt(maxTagLength - minTagLength + 1) + minTagLength;
    for (int i = 0; i < tagLength; i++) {
      int minWords = 1;
      int maxWords = 3;
      int wordCount = rand.nextInt(maxWords - minWords + 1) + minWords;
      joiner.add(wordList.getRandomPhrase(wordCount, false));
    }
    return joiner.toString();
  }

}
