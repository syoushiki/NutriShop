package com.healthshop.config;

import com.healthshop.entity.Product;
import com.healthshop.entity.User;
import com.healthshop.repo.ProductRepository;
import com.healthshop.repo.UserRepository;
import com.healthshop.security.Role;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;

@Configuration
public class DataLoader {
  @Bean
  CommandLineRunner load(ProductRepository repo, UserRepository userRepo, PasswordEncoder encoder) {
    return args -> {
      if (userRepo.count() == 0) {
        User admin = new User();
        admin.setUsername("admin");
        admin.setPassword(encoder.encode("admin123"));
        admin.setRole(Role.ADMIN);
        admin.setEmail("admin@healthshop.com");
        admin.setPhone("13800000000");
        userRepo.save(admin);

        User user = new User();
        user.setUsername("user");
        user.setPassword(encoder.encode("user123"));
        user.setRole(Role.USER);
        user.setEmail("user@healthshop.com");
        user.setPhone("13900000000");
        userRepo.save(user);
      }

      if (repo.count() == 0) {
        // 心血管分类
        Product p1 = new Product();
        p1.setName("深海鱼油软胶囊");
        p1.setDescription("高纯度Omega-3，辅助调节血脂，维护心血管健康。");
        p1.setImageUrl("https://placehold.co/600x400/2980b9/ffffff?text=Fish+Oil");
        p1.setPrice(new BigDecimal("199.00"));
        p1.setTags("cardio,nutrition");
        p1.setTargetAudience("all,31-50,50+");
        repo.save(p1);

        Product p2 = new Product();
        p2.setName("辅酶Q10");
        p2.setDescription("心脏动力源，增强心肌活力，抗氧化。");
        p2.setImageUrl("https://placehold.co/600x400/c0392b/ffffff?text=CoQ10");
        p2.setPrice(new BigDecimal("259.00"));
        p2.setTags("cardio,energy");
        p2.setTargetAudience("all,31-50,50+");
        repo.save(p2);

        // 护眼分类
        Product p3 = new Product();
        p3.setName("蓝莓叶黄素酯片");
        p3.setDescription("缓解眼疲劳，过滤蓝光，适合长期用眼人群。");
        p3.setImageUrl("https://placehold.co/600x400/8e44ad/ffffff?text=Lutein");
        p3.setPrice(new BigDecimal("89.00"));
        p3.setTags("eyes,sleep");
        p3.setTargetAudience("all,18-30,31-50");
        repo.save(p3);

        // 蛋白质/运动分类
        Product p4 = new Product();
        p4.setName("乳清蛋白粉");
        p4.setDescription("快速补充蛋白质，促进肌肉修复与生长。");
        p4.setImageUrl("https://placehold.co/600x400/27ae60/ffffff?text=Whey+Protein");
        p4.setPrice(new BigDecimal("328.00"));
        p4.setTags("fitness,protein,energy");
        p4.setTargetAudience("all,male,18-30,31-50");
        repo.save(p4);

        Product p5 = new Product();
        p5.setName("左旋肉碱");
        p5.setDescription("配合运动加速脂肪燃烧，提升耐力。");
        p5.setImageUrl("https://placehold.co/600x400/e67e22/ffffff?text=L-Carnitine");
        p5.setPrice(new BigDecimal("158.00"));
        p5.setTags("fitness,energy");
        p5.setTargetAudience("all,male,female,18-30,31-50");
        repo.save(p5);
        
        // 综合维矿
        Product p6 = new Product();
        p6.setName("多维元素片(男士)");
        p6.setDescription("专为男士设计，补充每日所需22种维生素矿物质。");
        p6.setImageUrl("https://placehold.co/600x400/f1c40f/333333?text=Multivitamin");
        p6.setPrice(new BigDecimal("119.00"));
        p6.setTags("nutrition,energy");
        p6.setTargetAudience("male,31-50,50+");
        repo.save(p6);
      }
    };
  }
}
