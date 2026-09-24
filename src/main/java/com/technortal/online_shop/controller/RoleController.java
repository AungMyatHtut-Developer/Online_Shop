package com.technortal.online_shop.controller;

import com.technortal.online_shop.dto.RoleFormDto;
import com.technortal.online_shop.dto.ValidationErrorDto;
import com.technortal.online_shop.exception.RoleNotFoundException;
import com.technortal.online_shop.exception.RoleValidationException;
import com.technortal.online_shop.service.RoleService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/roles")
public class RoleController {
    private final RoleService roles;
    public RoleController(RoleService roles) { this.roles = roles; }

    @InitBinder("role")
    public void bindRole(WebDataBinder binder) { binder.setAllowedFields("name", "description"); }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("roles", roles.getRoles());
        return "roles";
    }

    @GetMapping("/new")
    public String newRole(Model model) {
        model.addAttribute("role", new RoleFormDto());
        return form(model, null);
    }

    @PostMapping
    public String create(@ModelAttribute("role") RoleFormDto role, BindingResult errors, Model model, RedirectAttributes redirect) {
        if (errors.hasErrors()) return form(model, null);
        try {
            roles.createRole(role);
            redirect.addFlashAttribute("success", "Role created. Configure its menu access in Menu permission.");
            return "redirect:/roles";
        } catch (RoleValidationException ex) { addErrors(errors, ex); }
        catch (DataIntegrityViolationException ex) { errors.rejectValue("name", "duplicate", "This role name is already in use."); }
        return form(model, null);
    }

    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Long id, Model model) {
        var role = roles.getRole(id);
        var form = new RoleFormDto();
        form.setName(role.getName());
        form.setDescription(role.getDescription());
        model.addAttribute("role", form);
        return form(model, id);
    }

    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id, @ModelAttribute("role") RoleFormDto role, BindingResult errors,
                         Model model, RedirectAttributes redirect) {
        if (errors.hasErrors()) return form(model, id);
        try {
            roles.updateRole(id, role);
            redirect.addFlashAttribute("success", "Role updated successfully.");
            return "redirect:/roles";
        } catch (RoleValidationException ex) { addErrors(errors, ex); }
        catch (DataIntegrityViolationException ex) { errors.rejectValue("name", "duplicate", "This role name is already in use."); }
        return form(model, id);
    }

    @GetMapping("/{id}/delete")
    public String confirmDelete(@PathVariable Long id, Model model) {
        model.addAttribute("role", roles.getRole(id));
        return "role-delete";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            roles.deleteRole(id);
            redirect.addFlashAttribute("success", "Role deleted successfully.");
        } catch (RoleValidationException ex) { redirect.addFlashAttribute("error", ex.getErrors().getFirst().message()); }
        catch (DataIntegrityViolationException ex) { redirect.addFlashAttribute("error", "This role is assigned to users. Reassign those users before deleting it."); }
        return "redirect:/roles";
    }

    @ExceptionHandler(RoleNotFoundException.class)
    public ResponseEntity<Void> notFound() { return ResponseEntity.notFound().build(); }

    private String form(Model model, Long id) {
        model.addAttribute("roleId", id);
        model.addAttribute("systemRole", id != null && roles.getRole(id).isSystem());
        return "role-form";
    }

    private void addErrors(BindingResult errors, RoleValidationException ex) {
        for (ValidationErrorDto error : ex.getErrors()) {
            if (error.field() == null) errors.reject(error.code(), error.message());
            else errors.rejectValue(error.field(), error.code(), error.message());
        }
    }
}
