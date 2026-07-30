package com.notamethod.fluppy.core.game;

import com.notamethod.fluppy.gui.common.FileFormat;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface GameMapper {
    GameMapper INSTANCE = Mappers.getMapper(GameMapper.class);

    @Mapping(source = "gameExe", target = "game")
    @Mapping( target = "gameYear", source="year")
    @Mapping( target = "genres", ignore = true)
    GameEntity toEntity(GameApp gameApp);

    @Mapping(source = "game", target = "gameExe")
    @Mapping(source = "gameYear", target = "year")
    @Mapping(target = "publisher", ignore = true)

    @Named("toGameApp")
    @Mapping(source = "format", target = "format", qualifiedByName = "toFileFormat")
    GameApp toGameApp(GameEntity gameEntity);

    @ValueMapping(source = "amiga", target = "AMIGA")
    @ValueMapping(source = "dos", target = "DOS")
    @ValueMapping(source = "pc", target = "DOS")
    @ValueMapping(source = MappingConstants.ANY_REMAINING, target = "DOS")
    Platform toPlatform(String string);
    @Mapping(source = "game", target = "gameExe")
    @Mapping(source = "gameYear", target = "year")
    @Mapping(source = "format", target = "format", qualifiedByName = "toFileFormat")
    GameApp toFullGameApp(GameEntity gameEntity);
    GameApp copyGameApp(GameApp a);
    GenreEntity toEntity(GenreApp genre);

    @IterableMapping(qualifiedByName = "toGameApp")
    List<GameApp> toGameApps(List<GameEntity> entityList);

    @Named("StrYearConvert")
    default  Integer strYearConvert(String year ) {
        if (year==null)
            return null;
        try {
            return Integer.valueOf(year);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Named("toFileFormat")
    default FileFormat toFileFormat(String format) {
        if (format == null)
            return FileFormat.UNKNOWN;
        return FileFormat.fromValue(format);
    }

    default String map(Path path) {
        if (path==null){
            return null;
        }
        return path.toAbsolutePath().toString();
    }

    default Path map(String path) {
        if (path==null){
            return null;
        }
        return Paths.get(path);
    }

    GameApp toGameApp(GameApp a);


    Company toCompany(CompanyEntity companyEntity);
}
