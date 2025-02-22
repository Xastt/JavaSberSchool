package ru.xast.sbertasks.task17.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.xast.sbertasks.task17.models.Recipe;

import java.util.List;

@Repository
public interface RecipeRepository extends CrudRepository<Recipe, Integer> {
    List<Recipe> findByFullNameStartingWith(String name);
}
