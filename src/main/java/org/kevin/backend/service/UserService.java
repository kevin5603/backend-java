package org.kevin.backend.service;

import java.util.List;
import org.kevin.backend.model.User;
import org.kevin.backend.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {

  private final UserRepository userRepository;

  public UserService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  public List<User> getUsers() {
    return userRepository.findAll();
  }

  public User getUserByName(String username) {
    return userRepository.findByName(username)
      .orElseThrow(() -> new RuntimeException("User not found"));
  }

  public void saveUser(User user) {
    userRepository.save(user);
  }

  public void removeUser(Long id) {
    User user = userRepository.findById(id)
      .orElseThrow(() -> new RuntimeException("User not found"));
    userRepository.delete(user);
  }


}
