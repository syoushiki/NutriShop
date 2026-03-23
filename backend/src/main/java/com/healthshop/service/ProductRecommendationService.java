package com.healthshop.service;

import com.healthshop.entity.Product;
import com.healthshop.entity.UserProfile;
import com.healthshop.repo.ProductRepository;
import com.healthshop.repo.UserProfileRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ProductRecommendationService {
  private static final BigDecimal LOW_MAX = new BigDecimal("100");
  private static final BigDecimal MID_MIN = new BigDecimal("100");
  private static final BigDecimal MID_MAX = new BigDecimal("300");
  private static final BigDecimal HIGH_MIN = new BigDecimal("300");

  private final ProductRepository productRepository;
  private final UserProfileRepository userProfileRepository;

  public ProductRecommendationService(ProductRepository productRepository, UserProfileRepository userProfileRepository) {
    this.productRepository = productRepository;
    this.userProfileRepository = userProfileRepository;
  }

  public List<Product> recommend(Long userId) {
    List<Product> products = new ArrayList<>(productRepository.findAll());
    if (userId == null) {
      return products;
    }

    UserProfile profile = userProfileRepository.findByUserId(userId).orElse(null);
    if (profile == null || !hasSurvey(profile)) {
      return products;
    }

    Set<String> goals = parseTokenSet(profile.getHealthGoals());
    for (Product product : products) {
      product.setRelevanceScore(computeRelevanceScore(product, profile, goals));
    }

    products.sort(
        Comparator.comparing(Product::getRelevanceScore, Comparator.nullsLast(Comparator.reverseOrder()))
            .thenComparing(Product::getId, Comparator.nullsLast(Long::compareTo))
    );
    return products;
  }

  private boolean hasSurvey(UserProfile profile) {
    return notBlank(profile.getAgeGroup()) && notBlank(profile.getGender()) && notBlank(profile.getBudget()) && notBlank(profile.getHealthGoals());
  }

  private boolean notBlank(String value) {
    return value != null && !value.trim().isEmpty();
  }

  private double computeRelevanceScore(Product product, UserProfile profile, Set<String> goals) {
    Set<String> productTags = parseTokenSet(product.getTags());
    Set<String> audience = parseTokenSet(product.getTargetAudience());

    double tagScore = computeTagMatchScore(productTags, goals);
    double budgetScore = computeBudgetScore(product.getPrice(), profile.getBudget());
    double audienceScore = computeAudienceScore(audience, profile.getGender(), profile.getAgeGroup());
    double behaviorBoost = computeBehaviorBoost(productTags, profile);

    double score = 0.55 * tagScore + 0.25 * budgetScore + 0.20 * audienceScore + behaviorBoost;
    return clamp(score, 0.0, 1.0);
  }

  private double computeTagMatchScore(Set<String> productTags, Set<String> goals) {
    if (goals.isEmpty()) return 0.35;
    if (productTags.isEmpty()) return 0.1;
    long matched = goals.stream().filter(productTags::contains).count();
    return (double) matched / goals.size();
  }

  private double computeAudienceScore(Set<String> audience, String gender, String ageGroup) {
    if (audience.isEmpty()) return 0.6;
    String normalizedGender = normalize(gender);
    String normalizedAgeGroup = normalize(ageGroup);

    boolean hasGenderRule = containsAny(audience, "male", "female", "man", "woman", "unisex", "all");
    boolean hasAgeRule = containsAny(audience, "18-30", "31-50", "50+", "young", "adult", "senior", "all");

    double genderScore = 0.6;
    if (hasGenderRule) {
      if (audience.contains("all") || audience.contains("unisex")) {
        genderScore = 1.0;
      } else if (audience.contains(normalizedGender)) {
        genderScore = 1.0;
      } else {
        genderScore = 0.2;
      }
    }

    double ageScore = 0.6;
    if (hasAgeRule) {
      if (audience.contains("all") || audience.contains(normalizedAgeGroup)) {
        ageScore = 1.0;
      } else if ("18-30".equals(normalizedAgeGroup) && audience.contains("young")) {
        ageScore = 1.0;
      } else if ("31-50".equals(normalizedAgeGroup) && audience.contains("adult")) {
        ageScore = 1.0;
      } else if ("50+".equals(normalizedAgeGroup) && audience.contains("senior")) {
        ageScore = 1.0;
      } else {
        ageScore = 0.25;
      }
    }

    return (genderScore + ageScore) / 2.0;
  }

  private double computeBudgetScore(BigDecimal price, String budget) {
    if (price == null || budget == null || budget.isBlank()) return 0.5;

    String normalizedBudget = normalize(budget);
    if ("low".equals(normalizedBudget)) {
      if (price.compareTo(LOW_MAX) <= 0) return 1.0;
      BigDecimal overflow = price.subtract(LOW_MAX);
      return clamp(1.0 - overflow.divide(new BigDecimal("150"), 6, RoundingMode.HALF_UP).doubleValue(), 0.0, 1.0);
    }

    if ("mid".equals(normalizedBudget)) {
      if (price.compareTo(MID_MIN) >= 0 && price.compareTo(MID_MAX) <= 0) return 1.0;
      BigDecimal distance = price.compareTo(MID_MIN) < 0 ? MID_MIN.subtract(price) : price.subtract(MID_MAX);
      return clamp(1.0 - distance.divide(new BigDecimal("200"), 6, RoundingMode.HALF_UP).doubleValue(), 0.0, 1.0);
    }

    if ("high".equals(normalizedBudget)) {
      if (price.compareTo(HIGH_MIN) >= 0) return 1.0;
      BigDecimal gap = HIGH_MIN.subtract(price);
      return clamp(1.0 - gap.divide(new BigDecimal("300"), 6, RoundingMode.HALF_UP).doubleValue(), 0.0, 1.0);
    }

    return 0.5;
  }

  private double computeBehaviorBoost(Set<String> productTags, UserProfile profile) {
    if (productTags.isEmpty()) return 0.0;
    double boost = 0.0;

    String gender = normalize(profile.getGender());
    String ageGroup = normalize(profile.getAgeGroup());

    if ("male".equals(gender) && containsAny(productTags, "fitness", "protein", "energy", "cardio")) {
      boost += 0.08;
    }
    if ("female".equals(gender) && containsAny(productTags, "sleep", "eyes", "nutrition", "beauty")) {
      boost += 0.05;
    }
    if ("50+".equals(ageGroup) && containsAny(productTags, "cardio", "bone", "nutrition", "joints")) {
      boost += 0.06;
    }
    return boost;
  }

  private Set<String> parseTokenSet(String raw) {
    if (raw == null || raw.isBlank()) return Set.of();
    return java.util.Arrays.stream(raw.split("[,;/|\\s]+"))
        .map(this::normalize)
        .filter(s -> !s.isBlank())
        .collect(Collectors.toCollection(HashSet::new));
  }

  private boolean containsAny(Set<String> source, String... candidates) {
    for (String candidate : candidates) {
      if (source.contains(candidate)) return true;
    }
    return false;
  }

  private String normalize(String value) {
    return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
  }

  private double clamp(double value, double min, double max) {
    if (value < min) return min;
    if (value > max) return max;
    return value;
  }
}