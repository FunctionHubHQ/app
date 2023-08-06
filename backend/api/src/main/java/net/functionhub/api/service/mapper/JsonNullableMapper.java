package net.functionhub.api.service.mapper;

import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Named;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.util.ObjectUtils;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Biz Melesse
 * created on 8/18/22
 */
@Mapper(componentModel = "spring",
        unmappedTargetPolicy = org.mapstruct.ReportingPolicy.IGNORE,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface JsonNullableMapper {

    @Named("localDateTimeMapper")
    default LocalDateTime localDateTimeMapper(JsonNullable<Long> value) {
        if (value != null && value.isPresent() && value.get() != null) {
            return LocalDateTime.ofEpochSecond(value.get(), 0, ZoneOffset.UTC);
        }
        return null;
    }

    @Named("localDateTimeToDtoMapper")
    default JsonNullable<Long> localDateTimeToDtoMapper(LocalDateTime value) {
        if (value != null) {
            return JsonNullable.of(value.toEpochSecond(ZoneOffset.UTC));
        }
        return JsonNullable.of(null);
    }

    @Named("booleanMapper")
    default Boolean booleanMapper(JsonNullable<Boolean> value) {
        if (value != null && value.isPresent() && value.get() != null) {
            return value.get();
        }
        return false;
    }

    @Named("booleanToDtoMapper")
    default JsonNullable<Boolean> booleanToDtoMapper(Boolean value) {
        return JsonNullable.of(value);
    }

    @Named("longMapper")
    default Long longMapper(JsonNullable<Long> value) {
        if (value != null && value.isPresent() && value.get() != null) {
            return value.get();
        }
        return null;
    }

    @Named("longToDtoMapper")
    default JsonNullable<Long> longToDtoMapper(Long value) {
        return JsonNullable.of(value);
    }

    @Named("stringMapper")
    default String stringMapper(JsonNullable<String> value) {
        if (value != null && value.isPresent() && value.get() != null) {
            return value.get();
        }
        return null;
    }

    @Named("stringToDtoMapper")
    default JsonNullable<String> stringToDtoMapper(String value) {
        return JsonNullable.of(value);
    }

    @Named("listToArray")
    default Integer[] listToArray(JsonNullable<List<Long>> value) {
        if (value != null && value.isPresent() && value.get() != null) {
            return value.get()
                    .stream()
                    .map(Long::intValue).toArray(Integer[]::new);
        }
        return null;
    }

    @Named("listToDtoArray")
    default JsonNullable<List<Long>> listToDtoArray(Integer[] value) {
        if (!ObjectUtils.isEmpty(value)) {
            return JsonNullable.of(
                    Arrays.asList(value)
                            .stream().map(Integer::longValue)
                            .collect(Collectors.toList()));
        }
        return JsonNullable.of(new ArrayList<>());
    }

  @Named("objectsToString")
  default List<String> objectsToString(Object[] objects) {
    if (!ObjectUtils.isEmpty(objects)) {
      return Arrays
        .stream(objects)
        .map(Object::toString)
        .collect(Collectors.toList());
    }
    return new ArrayList<>();
  }
}
