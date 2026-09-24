package com.technortal.online_shop.controller;

import com.technortal.online_shop.dto.MenuPermissionFormDto;
import com.technortal.online_shop.dto.RoleDto;
import com.technortal.online_shop.exception.RoleNotFoundException;
import com.technortal.online_shop.exception.RoleValidationException;
import com.technortal.online_shop.security.PortalMenu;
import com.technortal.online_shop.security.PortalSession;
import com.technortal.online_shop.service.RoleService;
import com.technortal.online_shop.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.stream.Collectors;

@Controller
@RequestMapping("/menu-permissions")
public class MenuPermissionController {
    private final RoleService roles;
    private final UserService users;
    public MenuPermissionController(RoleService roles, UserService users) { this.roles = roles; this.users = users; }

    @InitBinder("permissions")
    public void bindPermissions(WebDataBinder binder) { binder.setAllowedFields("menus"); }

    @GetMapping
    public String show(@RequestParam(required = false) Long roleId, Model model) {
        var available = roles.getRoles();
        RoleDto selected = roleId == null ? available.getFirst() : roles.getRole(roleId);
        var form = new MenuPermissionFormDto();
        form.setMenus(selected.getMenus().stream().map(Enum::name).collect(Collectors.toSet()));
        model.addAttribute("permissions", form);
        return page(model, selected);
    }

    @PostMapping("/{id}")
    public String save(@PathVariable Long id, @ModelAttribute("permissions") MenuPermissionFormDto form,
                       BindingResult errors, Model model, HttpServletRequest request, RedirectAttributes redirect) {
        if (errors.hasErrors()) return page(model, roles.getRole(id));
        try {
            roles.updatePermissions(id, form);
            var current = users.getSessionUser(PortalSession.userId(request)).orElseThrow();
            PortalSession.refresh(request.getSession(false), current);
            redirect.addFlashAttribute("success", "Menu permissions updated successfully.");
            if (!current.user().getMenus().contains(PortalMenu.MENU_PERMISSIONS)) return "redirect:" + current.user().getHomePath();
            return "redirect:/menu-permissions?roleId=" + id;
        } catch (RoleValidationException ex) {
            ex.getErrors().forEach(error -> errors.reject(error.code(), error.message()));
            return page(model, roles.getRole(id));
        }
    }

    @ExceptionHandler(RoleNotFoundException.class)
    public ResponseEntity<Void> notFound() { return ResponseEntity.notFound().build(); }

    private String page(Model model, RoleDto selected) {
        model.addAttribute("roles", roles.getRoles());
        model.addAttribute("selectedRole", selected);
        model.addAttribute("menus", PortalMenu.values());
        return "menu-permissions";
    }
}
