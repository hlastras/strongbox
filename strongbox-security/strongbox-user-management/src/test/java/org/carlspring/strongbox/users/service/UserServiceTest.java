package org.carlspring.strongbox.users.service;

import org.carlspring.strongbox.config.DataServiceConfig;
import org.carlspring.strongbox.config.UsersConfig;
import org.carlspring.strongbox.users.domain.AccessModel;
import org.carlspring.strongbox.users.domain.User;
import org.carlspring.strongbox.users.dto.UserDto;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.stream.Collectors;

import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { DataServiceConfig.class,
                                  UsersConfig.class })
public class UserServiceTest
{

    private static final Logger logger = LoggerFactory.getLogger(UserServiceTest.class);

    @Inject
    UserService userService;

    @Before
    public void setup()
    {
        assertNotNull(userService);
    }

    @Test
    public void testFindByUsername()
    {
        // Load the user
        User user = userService.findByUserName("developer01");

        assertNotNull("Unable to find user by name developer01", user);
        assertNotNull("User authorities were not set!", user.getAuthorities());
        assertTrue("Expected user authorities to be grater than 0!", user.getAuthorities().size() > 0);

        User nullUser = userService.findByUserName(null);
        assertNull("User should have been null", nullUser);
    }

    @Test
    public void testCreate()
            throws Exception
    {
        String testUserName = "test-user";

        UserDto user = new UserDto();
        user.setEnabled(true);
        user.setUsername(testUserName);
        user.setPassword("test-password");
        user.setSecurityTokenKey("some-security-token");

        userService.add(user);

        User foundEntity = userService.findByUserName(testUserName);

        assertNotNull("Unable to locate user " + testUserName + ". Save operation failed!", foundEntity);

        logger.debug("Found stored user\n\t" + foundEntity + "\n");

        assertEquals(testUserName, foundEntity.getUsername());
        assertNotEquals("Expected a hashed password, received plain-text!", "test-password",
                        foundEntity.getPassword()); // password should NOT be saved as "plain"
        assertNotNull("User contains empty password!", foundEntity.getPassword());
        assertTrue(foundEntity.isEnabled());
        assertEquals("some-security-token", foundEntity.getSecurityTokenKey());
    }

    @Test
    public void testUpdateUser()
            throws Exception
    {
        String testUserName = "test-update-user";

        UserDto userAdd = new UserDto();
        userAdd.setEnabled(true);
        userAdd.setUsername(testUserName);
        userAdd.setPassword("test-password");
        userAdd.setSecurityTokenKey("before");

        userService.add(userAdd);

        User addedEntity = userService.findByUserName(testUserName);
        assertNotNull("Unable to locate user " + testUserName + ". Save operation failed!", addedEntity);

        logger.debug("Found stored user\n\t" + addedEntity + "\n");

        logger.debug("Updating user...");

        UserDto userUpdate = new UserDto();
        userUpdate.setUsername(testUserName);
        userUpdate.setPassword("another-password");
        userUpdate.setSecurityTokenKey("after");
        userUpdate.setEnabled(false);

        userService.updateByUsername(userUpdate);

        User updatedEntity = userService.findByUserName(testUserName);
        assertNotNull("Unable to locate updated user " + testUserName + ". Update operation failed!", updatedEntity);

        logger.debug("Found stored updated user\n\t" + updatedEntity + "\n");

        assertEquals(testUserName, updatedEntity.getUsername());
        assertNotEquals("Expected current password to have changed.", addedEntity.getPassword(),
                        updatedEntity.getPassword());
        assertNotNull("Expected password to be other than null", updatedEntity.getPassword());
        assertFalse("User should have been disabled, but is still enabled!", updatedEntity.isEnabled());
        assertEquals("after", updatedEntity.getSecurityTokenKey());
    }


    @Test
    public void testUpdatingUserWithEmptyAndBlankPasswordShouldNotUpdatePasswordField()
            throws Exception
    {

        // 1. add initial user
        String testUserName = "test-update-user-empty-blank-pass";

        UserDto userAdd = new UserDto();
        userAdd.setEnabled(true);
        userAdd.setUsername(testUserName);
        userAdd.setPassword("initial");

        userService.add(userAdd);

        User addedEntity = userService.findByUserName(testUserName);
        assertNotNull("Unable to locate user " + testUserName + ". Save operation failed!", addedEntity);

        logger.debug("Found stored initial user\n\t" + addedEntity + "\n");

        // 2. Update the user with empty/null password
        logger.debug("Updating user with empty/null pass...");

        UserDto userNullPassUpdate = new UserDto();
        userNullPassUpdate.setUsername(testUserName);
        userNullPassUpdate.setPassword(null);

        userService.updateByUsername(userNullPassUpdate);

        User updatedEntity = userService.findByUserName(testUserName);
        assertNotNull("Unable to locate updated user " + testUserName + ". Update operation failed!", updatedEntity);

        logger.debug("Found stored updated with empty pass user\n\t" + updatedEntity + "\n");

        assertEquals("User password has changed!", addedEntity.getPassword(), updatedEntity.getPassword());

        // 3. Update the user with blank password (i.e. contains only whitespace)
        logger.debug("Updating user with blank pass...");

        UserDto userBlankPassUpdate = new UserDto();
        userBlankPassUpdate.setUsername(testUserName);
        userBlankPassUpdate.setPassword(null);

        userService.updateByUsername(userBlankPassUpdate);

        updatedEntity = userService.findByUserName(testUserName);
        assertNotNull("Unable to locate updated user " + testUserName + ". Update operation failed!", updatedEntity);

        logger.debug("Found stored updated with empty pass user\n\t" + updatedEntity + "\n");

        assertEquals("User password has changed!", addedEntity.getPassword(), updatedEntity.getPassword());
    }

    @Test
    public void testUpdateUserAccountDetails()
            throws Exception
    {
        String testUserName = "test-update-user-account";

        UserDto userAdd = new UserDto();
        userAdd.setEnabled(true);
        userAdd.setUsername(testUserName);
        userAdd.setPassword("test-password");
        userAdd.setSecurityTokenKey("before");

        userService.add(userAdd);

        User addedEntity = userService.findByUserName(testUserName);
        assertNotNull("Unable to locate user " + testUserName + ". Save operation failed!", addedEntity);

        logger.debug("Found stored user\n\t" + addedEntity + "\n");

        logger.debug("Updating user...");

        UserDto userUpdate = new UserDto();
        userUpdate.setUsername(testUserName);
        userUpdate.setPassword("another-password");
        userUpdate.setSecurityTokenKey("after");
        userUpdate.setEnabled(false);
        userUpdate.setRoles(new HashSet<>(Arrays.asList("a", "b")));

        userService.updateAccountDetailsByUsername(userUpdate);

        User updatedEntity = userService.findByUserName(testUserName);
        assertNotNull("Unable to locate updated user " + testUserName + ". Update operation failed!", updatedEntity);

        logger.debug("Updated user found: \n\t" + updatedEntity + "\n");

        assertNotEquals("User password should have been encrypted!", addedEntity.getPassword(),
                        updatedEntity.getPassword());
        assertNotNull("User password was updated to null", updatedEntity.getPassword());
        assertTrue(updatedEntity.isEnabled());
        assertEquals("Expected no user roles to have been updated!", 0, updatedEntity.getRoles().size());
        assertEquals("after", updatedEntity.getSecurityTokenKey());
    }

    @Test
    public void testThatUserNameIsUnique()
    {
        assertThat(userService.findAll().getUsers().stream().filter(u -> "admin".equals(u.getUsername())).collect(
                Collectors.toList()).size(), CoreMatchers.equalTo(1));

        UserDto user = new UserDto();
        user.setUsername("admin");

        userService.add(user);

        assertThat(userService.findAll().getUsers().stream().filter(u -> "admin".equals(u.getUsername())).collect(
                Collectors.toList()).size(), CoreMatchers.equalTo(1));
    }

    @Test
    public void testPrivilegesProcessingForAccessModel()
    {
        // Load the user
        User user = userService.findByUserName("developer01");

        assertNotNull("Unable to find user by name developer01", user);

        // Display the access model
        AccessModel accessModel = user.getAccessModel();

        logger.debug(accessModel.toString());

        // Make sure that the privileges were correctly assigned for the example paths
        Collection<String> privileges;

        privileges = accessModel.getPathPrivileges("/storages/storage0/releases/" +
                                                   "org/carlspring/foo/1.1/foo-1.1.jar");

        assertNotNull(privileges);
        assertFalse(privileges.isEmpty());
        assertThat(privileges.size(), CoreMatchers.equalTo(2));
        assertTrue(privileges.contains("ARTIFACTS_RESOLVE"));
        assertTrue(privileges.contains("ARTIFACTS_DELETE"));

        privileges = accessModel.getPathPrivileges("/storages/storage0/releases/" +
                                                   "com/carlspring/foo/1.2/foo-1.2.jar");

        assertNotNull(privileges);
        assertFalse(privileges.isEmpty());
        assertThat(privileges.size(), CoreMatchers.equalTo(2));
        assertTrue(privileges.contains("ARTIFACTS_RESOLVE"));
        assertTrue(privileges.contains("ARTIFACTS_VIEW"));

        privileges = accessModel.getPathPrivileges("/storages/storage0/releases/" +
                                                   "com/mycorp/foo/1.2/foo-1.2.jar");

        assertNotNull(privileges);
        assertFalse(privileges.isEmpty());
        assertThat(privileges.size(), CoreMatchers.equalTo(1));
        assertTrue(privileges.contains("ARTIFACTS_RESOLVE"));

        privileges = accessModel.getPathPrivileges("/storages/storage0/releases/" +
                                                   "com/mycorp/");

        assertNotNull(privileges);
        assertFalse(privileges.isEmpty());
        assertThat(privileges.size(), CoreMatchers.equalTo(5));
        assertTrue(privileges.contains("ARTIFACTS_RESOLVE"));
        assertTrue(privileges.contains("ARTIFACTS_VIEW"));
        assertTrue(privileges.contains("ARTIFACTS_DEPLOY"));
        assertTrue(privileges.contains("ARTIFACTS_DELETE"));
        assertTrue(privileges.contains("ARTIFACTS_COPY"));
    }

    @Test
    public void testDeleteUser()
            throws Exception
    {
        String testUserName = "test-delete-user";

        UserDto userAdd = new UserDto();
        userAdd.setEnabled(true);
        userAdd.setUsername(testUserName);
        userAdd.setPassword("test-password");
        userAdd.setSecurityTokenKey("before");

        userService.add(userAdd);

        User addedEntity = userService.findByUserName(testUserName);
        assertNotNull("Unable to locate user " + testUserName + ". Delete operation failed!", addedEntity);

        logger.debug("Found stored user\n\t" + addedEntity + "\n");

        logger.debug("Deleting user...");

        userService.delete(testUserName);

        User deletedEntity = userService.findByUserName(testUserName);
        assertNull("User " + testUserName + " is still present in the database. Delete operation failed!",
                   deletedEntity);
    }

}
