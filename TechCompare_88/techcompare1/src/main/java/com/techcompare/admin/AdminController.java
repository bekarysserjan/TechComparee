package com.techcompare.admin;

import com.techcompare.laptop.Laptop;
import com.techcompare.laptop.LaptopRepository;
import com.techcompare.smartphone.Smartphone;
import com.techcompare.smartphone.SmartphoneRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
public class AdminController {
    private final SmartphoneRepository smartphones;
    private final LaptopRepository laptops;

    public AdminController(SmartphoneRepository smartphones, LaptopRepository laptops) {
        this.smartphones = smartphones;
        this.laptops = laptops;
    }

    @GetMapping({"", "/"})
    public String dashboard(Model model) {
        model.addAttribute("smartphoneCount", smartphones.count());
        model.addAttribute("laptopCount", laptops.count());
        return "admin/dashboard";
    }

    @GetMapping("/smartphones")
    public String smartphoneList(Model model) {
        model.addAttribute("devices", smartphones.findAll());
        model.addAttribute("deviceType", "smartphone");
        return "admin/list";
    }

    @GetMapping("/laptops")
    public String laptopList(Model model) {
        model.addAttribute("devices", laptops.findAll());
        model.addAttribute("deviceType", "laptop");
        return "admin/list";
    }

    @GetMapping("/smartphones/new")
    public String newSmartphone(Model model) {
        model.addAttribute("device", new Smartphone());
        model.addAttribute("formTitle", "Add smartphone");
        model.addAttribute("formAction", "/admin/smartphones");
        return "admin/smartphone-form";
    }

    @PostMapping("/smartphones")
    public String createSmartphone(@ModelAttribute("device") Smartphone device,
                                   BindingResult result, RedirectAttributes redirect, Model model) {
        if (result.hasErrors() || blank(device.getName()) || blank(device.getBrand())) {
            model.addAttribute("formTitle", "Add smartphone");
            model.addAttribute("formAction", "/admin/smartphones");
            return "admin/smartphone-form";
        }
        device.setId(null);
        smartphones.save(device);
        redirect.addFlashAttribute("message", "Smartphone added successfully.");
        return "redirect:/admin/smartphones";
    }

    @GetMapping("/smartphones/{id}/edit")
    public String editSmartphone(@PathVariable Integer id, Model model) {
        model.addAttribute("device", smartphones.findById(id).orElseThrow());
        model.addAttribute("formTitle", "Edit smartphone");
        model.addAttribute("formAction", "/admin/smartphones/" + id);
        return "admin/smartphone-form";
    }

    @PostMapping("/smartphones/{id}")
    public String updateSmartphone(@PathVariable Integer id, @ModelAttribute("device") Smartphone form,
                                   BindingResult result, RedirectAttributes redirect, Model model) {
        if (result.hasErrors() || blank(form.getName()) || blank(form.getBrand())) {
            model.addAttribute("formTitle", "Edit smartphone");
            model.addAttribute("formAction", "/admin/smartphones/" + id);
            return "admin/smartphone-form";
        }
        Smartphone device = smartphones.findById(id).orElseThrow();
        device.setName(form.getName()); device.setBrand(form.getBrand()); device.setImageUrl(form.getImageUrl());
        device.setGeekbenchMulti(form.getGeekbenchMulti()); device.setGeekbenchSingle(form.getGeekbenchSingle());
        device.setAntutuGpuScore(form.getAntutuGpuScore()); device.setRamGb(form.getRamGb());
        device.setStorageGb(form.getStorageGb()); device.setBatteryMah(form.getBatteryMah());
        device.setMainCameraMp(form.getMainCameraMp()); device.setHasTelephotoLens(form.getHasTelephotoLens());
        device.setVideoMaxFps(form.getVideoMaxFps()); device.setFrontCameraMp(form.getFrontCameraMp());
        device.setScreenRefreshHz(form.getScreenRefreshHz()); device.setScreenBrightnessNits(form.getScreenBrightnessNits());
        device.setWeightG(form.getWeightG()); device.setHas5g(form.getHas5g());
        smartphones.save(device);
        redirect.addFlashAttribute("message", "Smartphone updated successfully.");
        return "redirect:/admin/smartphones";
    }

    @PostMapping("/smartphones/{id}/delete")
    public String deleteSmartphone(@PathVariable Integer id, RedirectAttributes redirect) {
        smartphones.deleteById(id);
        redirect.addFlashAttribute("message", "Smartphone deleted.");
        return "redirect:/admin/smartphones";
    }

    @GetMapping("/laptops/new")
    public String newLaptop(Model model) {
        model.addAttribute("device", new Laptop());
        model.addAttribute("formTitle", "Add laptop");
        model.addAttribute("formAction", "/admin/laptops");
        return "admin/laptop-form";
    }

    @PostMapping("/laptops")
    public String createLaptop(@ModelAttribute("device") Laptop device, BindingResult result,
                               RedirectAttributes redirect, Model model) {
        if (result.hasErrors() || blank(device.getName()) || blank(device.getBrand())) {
            model.addAttribute("formTitle", "Add laptop");
            model.addAttribute("formAction", "/admin/laptops");
            return "admin/laptop-form";
        }
        device.setId(null);
        laptops.save(device);
        redirect.addFlashAttribute("message", "Laptop added successfully.");
        return "redirect:/admin/laptops";
    }

    @GetMapping("/laptops/{id}/edit")
    public String editLaptop(@PathVariable Integer id, Model model) {
        model.addAttribute("device", laptops.findById(id).orElseThrow());
        model.addAttribute("formTitle", "Edit laptop");
        model.addAttribute("formAction", "/admin/laptops/" + id);
        return "admin/laptop-form";
    }

    @PostMapping("/laptops/{id}")
    public String updateLaptop(@PathVariable Integer id, @ModelAttribute("device") Laptop form,
                               BindingResult result, RedirectAttributes redirect, Model model) {
        if (result.hasErrors() || blank(form.getName()) || blank(form.getBrand())) {
            model.addAttribute("formTitle", "Edit laptop");
            model.addAttribute("formAction", "/admin/laptops/" + id);
            return "admin/laptop-form";
        }
        Laptop device = laptops.findById(id).orElseThrow();
        device.setName(form.getName()); device.setBrand(form.getBrand()); device.setImageUrl(form.getImageUrl());
        device.setGeekbenchMulti(form.getGeekbenchMulti()); device.setGeekbenchSingle(form.getGeekbenchSingle());
        device.setGpuBenchmarkScore(form.getGpuBenchmarkScore()); device.setRamGb(form.getRamGb());
        device.setStorageGb(form.getStorageGb()); device.setScreenResolutionPixels(form.getScreenResolutionPixels());
        device.setScreenSizeInch(form.getScreenSizeInch()); device.setPanelType(form.getPanelType());
        device.setHasTouchscreen(form.getHasTouchscreen()); device.setWeightKg(form.getWeightKg());
        device.setBatteryLifeHours(form.getBatteryLifeHours());
        laptops.save(device);
        redirect.addFlashAttribute("message", "Laptop updated successfully.");
        return "redirect:/admin/laptops";
    }

    @PostMapping("/laptops/{id}/delete")
    public String deleteLaptop(@PathVariable Integer id, RedirectAttributes redirect) {
        laptops.deleteById(id);
        redirect.addFlashAttribute("message", "Laptop deleted.");
        return "redirect:/admin/laptops";
    }

    private boolean blank(String value) { return value == null || value.isBlank(); }
}
