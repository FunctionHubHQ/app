package net.functionhub.api.utils;

import net.functionhub.api.UserProfile;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import net.functionhub.api.dto.SessionUser;
import org.mockito.Mockito;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.nio.file.Files;

/**
 * @author Bizuwork Melesse
 * created on 2/17/22
 */
@Service
@RequiredArgsConstructor
public class ServiceTestHelper {

    public void prepareSecurity(String userId) {
        Authentication authentication = Mockito.mock(Authentication.class);
        SessionUser user = new SessionUser();
        user.setName("TestNG User");
        user.setEmail("testuser@exampe.com");
        user.setUid(userId == null ? UUID.randomUUID().toString() : userId);
        Mockito.when(authentication.getPrincipal()).thenReturn(user);

        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    public String loadFile(String path) {
        File file = null;
        try {
            file = ResourceUtils.getFile(path);
            try {
                return new String(Files.readAllBytes(file.toPath()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public String loadFromFile(String path) {
        InputStream resource = null;
        try {
            resource = new ClassPathResource(path).getInputStream();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource)) ) {
            return FileCopyUtils.copyToString(reader);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
