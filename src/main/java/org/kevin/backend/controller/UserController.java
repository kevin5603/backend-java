package org.kevin.backend.controller;

import java.util.List;
import org.kevin.backend.model.User;
import org.kevin.backend.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("users")
public class UserController {

  private final UserService userService;

  public UserController(UserService userService) {
    this.userService = userService;
  }


  @GetMapping
  public ResponseEntity<List<User>> getUsers() {
    System.out.println("get all users !!!");
    List<User> users = userService.getUsers();
    return ResponseEntity
      .status(HttpStatus.OK)
      .body(users);
  }

  @GetMapping("/{userName}")
  public ResponseEntity<User> getUserByName(@PathVariable("userName") String userName) {
    System.out.println("find user by name: " + userName);
    User user = userService.getUserByName(userName);
    return ResponseEntity
      .status(HttpStatus.OK)
      .body(user);
  }

  @PostMapping
  public ResponseEntity saveUser(@RequestBody User user) {
    System.out.println("save user: " + user);
    userService.saveUser(user);
    return ResponseEntity
      .status(HttpStatus.CREATED)
      .build();
  }

  @DeleteMapping("/{id}")
  public ResponseEntity removeUser(@PathVariable Long id) {
    System.out.println("remove user by id: " + id);
    userService.removeUser(id);
    return ResponseEntity
      .status(HttpStatus.OK)
      .build();
  }

  @ExceptionHandler(RuntimeException.class)
  public ResponseEntity<String> handleIllegalArgumentException(RuntimeException ex) {
    return ResponseEntity
      .status(HttpStatus.BAD_REQUEST)
      .body("found exception: " + ex.getMessage());
  }


}
