package com.zjj.main.interfaces.mvc.resource.menu.transformation;

import com.zjj.main.infrastructure.db.jpa.entity.MenuResource;
import com.zjj.main.interfaces.mvc.resource.menu.vo.MenuResourceVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.util.CollectionUtils;

import java.util.LinkedList;
import java.util.List;

/**
 * @author zengJiaJun
 * @version 1.0
 * @crateTime 2025年04月09日 16:57
 */
@Mapper(componentModel = "spring")
public interface MenuResourceVOTransformation {


    @Mapping(target = "parentId", source = "parentMenu.id")
    @Mapping(target = "children", expression = "java(this.to(menuResource.getChileMenus()))")
    public MenuResourceVO to(MenuResource menuResource);


    default List<MenuResourceVO> to(List<MenuResource> menuResources) {
        if (CollectionUtils.isEmpty(menuResources)) {
            return null;
        }
        LinkedList<MenuResourceVO> result = new LinkedList<>();
        for (MenuResource menuResource : menuResources) {
            result.add(this.to(menuResource));
        }
        return result;
    }

    @Mapping(target = "parentId", source = "parentMenu.id")
    @Mapping(target = "children", expression = "java(this.toFilterButton(menuResource.getChileMenus()))")
    MenuResourceVO toFilterButton(MenuResource menuResource);

    default List<MenuResourceVO> toFilterButton(List<MenuResource> menuResources) {
        if (CollectionUtils.isEmpty(menuResources)) {
            return null;
        }
        LinkedList<MenuResourceVO> result = new LinkedList<>();
        for (MenuResource menuResource : menuResources) {
            if ("BUTTON".equals(menuResource.getType())) continue;
            result.add(this.to(menuResource));
        }
        return result;
    }
}
