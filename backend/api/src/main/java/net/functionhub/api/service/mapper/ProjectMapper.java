package net.functionhub.api.service.mapper;

import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import net.functionhub.api.FHFunction;
import net.functionhub.api.Project;
import net.functionhub.api.data.postgres.entity.CodeCellEntity;
import net.functionhub.api.data.postgres.entity.ProjectEntity;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.springframework.util.ObjectUtils;

/**
 * @author Biz Melesse
 * created on 8/17/23
 */
@Mapper(componentModel = "spring",
        unmappedTargetPolicy = org.mapstruct.ReportingPolicy.IGNORE,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface ProjectMapper {

    default Project mapFromProjectEntity(ProjectEntity entity) {
        if (entity == null) {
            return null;
        }
        return new Project()
            .projectId(entity.getUid().toString())
            .name(entity.getProjectName())
            .description(entity.getDescription())
            .createdAt(entity.getCreatedAt().toEpochSecond(ZoneOffset.UTC))
            .updatedAt(entity.getUpdatedAt().toEpochSecond(ZoneOffset.UTC));
    }

    default List<Project> mapFromProjectEntities(List<ProjectEntity> entities) {
        if (ObjectUtils.isEmpty(entities)) {
            return new ArrayList<>();
        }
        return entities
            .stream()
            .map(this::mapFromProjectEntity).
            collect(Collectors.toList());
    }

    default FHFunction mapFromCodeCellEntity(CodeCellEntity entity) {
        if (entity == null) {
            return null;
        }
        return new FHFunction()
            .createdAt(entity.getCreatedAt().toEpochSecond(ZoneOffset.UTC))
            .updatedAt(entity.getUpdatedAt().toEpochSecond(ZoneOffset.UTC))
            .isPublic(entity.getIsPublic())
            .name(entity.getFunctionName())
            .slug(entity.getSlug())
            .codeId(entity.getUid().toString())
            .description(entity.getDescription());
    }

    default List<FHFunction> mapFromCodeCellEntities(List<CodeCellEntity> entities) {
        if (ObjectUtils.isEmpty(entities)) {
            return new ArrayList<>();
        }
        return entities
            .stream()
            .map(this::mapFromCodeCellEntity).
            collect(Collectors.toList());
    }
}
