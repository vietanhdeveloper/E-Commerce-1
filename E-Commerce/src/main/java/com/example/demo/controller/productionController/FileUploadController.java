package com.example.demo.controller.productionController;

import com.example.demo.production.Product;
import com.example.demo.repository.BrandRepository;
import com.example.demo.repository.CategoryRepository;
import com.example.demo.repository.ProductRepository;
import com.example.demo.storage.StorageFileNotFoundException;
import com.example.demo.storage.service.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

@Controller
public class FileUploadController {
    @Autowired
    private final StorageService storageService;
    @Autowired
    private final ProductRepository productRepository;
    @Autowired
    private final CategoryRepository categoryRepository;
    @Autowired
    private final BrandRepository brandRepository;

    public FileUploadController(StorageService storageService, ProductRepository productRepository, CategoryRepository categoryRepository, BrandRepository brandRepository) {
        this.storageService = storageService;
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.brandRepository = brandRepository;
    }
    @GetMapping("/admin/products/{id}/add-img")
    public String listUploadedFiles(Model model,
                                    @PathVariable("id") long id) throws IOException {
        model.addAttribute("action","/admin/products/{id}/add-img" + id);
        return "image/uploadForm";
    }

    @PostMapping("/admin/products/{id}/add-img")
    public String handleFileUpload(@RequestParam("file") MultipartFile file,
                                   @PathVariable("id") long id,
                                   Model model,
                                   HttpServletRequest req) throws IOException
    {
        storageService.store(file);
        Optional<Product> pros = productRepository.findById(id);
        if (pros.isPresent()){
            Product pro = pros.get();
            String[] temp = req.getRequestURL().toString().split("//");
            String[] value = temp[1].split("/");
            String mainURL = temp[0] + "//" + value[0];
            pro.setLinkImg(mainURL + "/files/" + file.getOriginalFilename());
            productRepository.save(pro);
        }
        model.addAttribute("products", productRepository.findAll());
        return "redirect:/admin/products/";
    }

    @GetMapping("/files/{filename:.+}")
    @ResponseBody
    public ResponseEntity<UrlResource> serveFile(@PathVariable String filename) {

        UrlResource file = storageService.loadAsResource(filename);
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + file.getFilename() + "\"").body(file);
    }

    @ExceptionHandler(StorageFileNotFoundException.class)
    public ResponseEntity<?> handleStorageFileNotFound(StorageFileNotFoundException exc) {
        return ResponseEntity.notFound().build();
    }

}
