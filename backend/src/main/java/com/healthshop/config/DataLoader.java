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

      // cardio: low / mid / high
      seed(repo, "鱼油基础款", "日常心血管营养补充，适合入门人群。", "https://placehold.co/600x400/1d4ed8/ffffff?text=Fish+Oil+Basic", "89.00", "cardio,nutrition", "all,18-30,31-50");
      seed(repo, "深海鱼油进阶款", "高纯度 Omega-3，辅助调节血脂。", "https://placehold.co/600x400/0f766e/ffffff?text=Fish+Oil+Pro", "199.00", "cardio,nutrition", "all,31-50,50+");
      seed(repo, "鱼油尊享装", "更高浓度 EPA/DHA，适合品质需求人群。", "https://placehold.co/600x400/0b3b66/ffffff?text=Fish+Oil+Ultra", "389.00", "cardio,nutrition", "all,31-50,50+");

      // sleep: low / mid / high
      seed(repo, "褪黑素轻量版", "帮助缩短入睡时间，温和改善睡眠。", "https://placehold.co/600x400/334155/ffffff?text=Sleep+Lite", "69.00", "sleep,calm", "all,18-30,31-50");
      seed(repo, "睡眠修复片", "复合成分配方，提升深睡眠质量。", "https://placehold.co/600x400/475569/ffffff?text=Sleep+Pro", "169.00", "sleep,nutrition", "all,31-50,50+");
      seed(repo, "睡眠高端修护装", "高纯植物舒缓配方，面向高端睡眠需求。", "https://placehold.co/600x400/1e293b/ffffff?text=Sleep+Ultra", "359.00", "sleep,nutrition", "all,female,male,31-50");

      // eyes: low / mid / high
      seed(repo, "叶黄素基础版", "缓解眼疲劳，适合长期用眼。", "https://placehold.co/600x400/7c3aed/ffffff?text=Eyes+Lite", "79.00", "eyes", "all,18-30,31-50");
      seed(repo, "蓝莓叶黄素复合片", "过滤蓝光，缓解干涩与视疲劳。", "https://placehold.co/600x400/6d28d9/ffffff?text=Eyes+Pro", "189.00", "eyes,nutrition", "all,18-30,31-50");
      seed(repo, "护眼高端配方", "多重抗氧化营养，面向深度护眼需求。", "https://placehold.co/600x400/4c1d95/ffffff?text=Eyes+Ultra", "329.00", "eyes,nutrition", "all,31-50,50+");

      // fitness: low / mid / high
      seed(repo, "运动能量凝胶", "训练前后快速补能，轻负担。", "https://placehold.co/600x400/f97316/ffffff?text=Fitness+Lite", "99.00", "fitness,energy", "all,18-30");
      seed(repo, "乳清蛋白粉", "促进肌肉修复与生长。", "https://placehold.co/600x400/22c55e/ffffff?text=Fitness+Pro", "238.00", "fitness,protein,energy", "all,male,female,18-30,31-50");
      seed(repo, "健身增肌尊享组合", "高蛋白+支链氨基酸，面向高阶训练。", "https://placehold.co/600x400/15803d/ffffff?text=Fitness+Ultra", "498.00", "fitness,protein,energy", "all,male,18-30,31-50");

      // nutrition / beauty / joints high differentiation
      seed(repo, "多维元素片（男士）", "专为男士设计，补充日常维生素和矿物质。", "https://placehold.co/600x400/f59e0b/333333?text=Multivitamin+Men", "119.00", "nutrition,energy", "male,31-50,50+");
      seed(repo, "多维元素片（女士）", "面向女性日常营养补充。", "https://placehold.co/600x400/f43f5e/ffffff?text=Multivitamin+Women", "129.00", "nutrition,beauty", "female,18-30,31-50");
      seed(repo, "胶原蛋白肽高端款", "精致养护与抗氧化支持。", "https://placehold.co/600x400/e11d48/ffffff?text=Collagen+Ultra", "458.00", "beauty,nutrition", "female,31-50,50+");
      seed(repo, "关节灵活配方", "针对中老年关节活动支持。", "https://placehold.co/600x400/6b4f3a/ffffff?text=Joint+Support", "268.00", "joints,bone,cardio", "all,50+");
    };
  }

  private void seed(ProductRepository repo,
                    String name,
                    String description,
                    String imageUrl,
                    String price,
                    String tags,
                    String targetAudience) {
    boolean exists = repo.findAll().stream().anyMatch(p -> name.equals(p.getName()));
    if (exists) return;

    Product product = new Product();
    product.setName(name);
    product.setDescription(description);
    product.setImageUrl(imageUrl);
    product.setPrice(new BigDecimal(price));
    product.setTags(tags);
    product.setTargetAudience(targetAudience);
    repo.save(product);
  }
}