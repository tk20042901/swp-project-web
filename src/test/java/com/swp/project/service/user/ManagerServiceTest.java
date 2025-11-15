package com.swp.project.service.user;

import com.swp.project.dto.EditManagerDto;
import com.swp.project.dto.CreateManagerDto;
import com.swp.project.entity.address.CommuneWard;
import com.swp.project.entity.address.ProvinceCity;
import com.swp.project.entity.user.Manager;
import com.swp.project.entity.user.Seller;
import com.swp.project.entity.user.Shipper;
import com.swp.project.listener.event.UserDisabledEvent;
import com.swp.project.repository.address.CommuneWardRepository;
import com.swp.project.repository.user.ManagerRepository;
import com.swp.project.repository.user.SellerRepository;
import com.swp.project.repository.user.ShipperRepository;
import com.swp.project.repository.user.UserRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.time.LocalDate;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ExtendWith(MockitoExtension.class)
class ManagerServiceTest {

    @Mock
    private ManagerRepository managerRepository;
    
    @Mock
    private ApplicationEventPublisher eventPublisher;
    
    @Mock
    private PasswordEncoder passwordEncoder;
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private CommuneWardRepository communeWardRepository;
    
    @Mock
    private SellerRepository sellerRepository;
    
    @Mock
    private ShipperRepository shipperRepository;
    
    @InjectMocks
    private ManagerService managerService;

    private CreateManagerDto validRegisterDto;
    private CommuneWard mockCommuneWard;
    private Manager existingManager;
    private EditManagerDto validEditManagerDto;
    private ProvinceCity mockProvinceCity;

    @BeforeEach
    void setUp() {
        // Setup valid register DTO
        validRegisterDto = new CreateManagerDto();
        validRegisterDto.setEmail("manager@test.com");
        validRegisterDto.setPassword("password123");
        validRegisterDto.setFullname("Test Manager");
        validRegisterDto.setBirthDate(LocalDate.of(1990, 1, 1));
        validRegisterDto.setCId("123456789");
//        validRegisterDto.setCommuneWardCode("WARD001");
        validRegisterDto.setSpecificAddress("123 Test Street");
        
        // Setup mock province city
        mockProvinceCity = new ProvinceCity();
        mockProvinceCity.setCode("CITY001");
        mockProvinceCity.setName("Test City");
        
        // Setup mock commune ward
        mockCommuneWard = new CommuneWard();
        mockCommuneWard.setCode("WARD001");
        mockCommuneWard.setName("Test Ward");
        mockCommuneWard.setProvinceCity(mockProvinceCity);
        
        // Setup existing manager
        existingManager = Manager.builder()
            .id(1L)
            .email("existing@test.com")
            .password("encodedPassword")
            .fullname("Existing Manager")
            .birthDate(LocalDate.of(1985, 5, 15))
            .cid("987654321")
            .communeWard(mockCommuneWard)
            .specificAddress("456 Old Street")
            .enabled(true)
            .build();
        
        // Setup valid edit manager DTO
        validEditManagerDto = new EditManagerDto(existingManager);
        validEditManagerDto.setEmail("updated@test.com");
        validEditManagerDto.setFullname("Updated Manager");
        validEditManagerDto.setBirthDate(LocalDate.of(1990, 6, 20));
        validEditManagerDto.setCId("111222333");
//        validEditManagerDto.setCommuneWardCode("WARD001");
        validEditManagerDto.setSpecificAddress("789 New Street");
        validEditManagerDto.setStatus(true);
    }

    @Test
    @DisplayName("Happy case: Tạo manager thành công với dữ liệu hợp lệ")
    @Order(1)
    void createManager_Success_ValidData() {
        // Arrange
//        when(communeWardRepository.findById(validRegisterDto.getCommuneWardCode()))
//            .thenReturn(Optional.of(mockCommuneWard));
        when(userRepository.existsByEmail(validRegisterDto.getEmail())).thenReturn(false);
        when(sellerRepository.findByCid(validRegisterDto.getCId())).thenReturn(null);
        when(shipperRepository.findByCid(validRegisterDto.getCId())).thenReturn(null);
        when(managerRepository.findByCid(validRegisterDto.getCId())).thenReturn(null);
        when(passwordEncoder.encode(validRegisterDto.getPassword())).thenReturn("encodedPassword");
        
        // Act
        assertDoesNotThrow(() -> managerService.createManager(validRegisterDto));
        
        // Assert
        ArgumentCaptor<Manager> managerCaptor = ArgumentCaptor.forClass(Manager.class);
        verify(managerRepository).save(managerCaptor.capture());
        
        Manager savedManager = managerCaptor.getValue();
        assertEquals(validRegisterDto.getEmail(), savedManager.getEmail());
        assertEquals("encodedPassword", savedManager.getPassword());
        assertEquals(validRegisterDto.getFullname(), savedManager.getFullname());
        assertEquals(validRegisterDto.getBirthDate(), savedManager.getBirthDate());
        assertEquals(validRegisterDto.getCId(), savedManager.getCid());
        assertEquals(mockCommuneWard, savedManager.getCommuneWard());
        assertEquals(validRegisterDto.getSpecificAddress(), savedManager.getSpecificAddress());
        
        verify(passwordEncoder).encode(validRegisterDto.getPassword());
//        verify(communeWardRepository).findById(validRegisterDto.getCommuneWardCode());
        verify(userRepository).existsByEmail(validRegisterDto.getEmail());
        verify(sellerRepository).findByCid(validRegisterDto.getCId());
        verify(shipperRepository).findByCid(validRegisterDto.getCId());
        verify(managerRepository).findByCid(validRegisterDto.getCId());
    }

    @Test
    @DisplayName("createManager: Thất bại khi không tìm thấy xã")
    @Order(2)
    void createManager_Fail_CommuneWardNotFound() {
        // Arrange
//        when(communeWardRepository.findById(validRegisterDto.getCommuneWardCode()))
//            .thenReturn(Optional.empty());
        
        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> managerService.createManager(validRegisterDto));
        
        assertEquals("Không tìm thấy xã", exception.getMessage());
        verify(managerRepository, never()).save(any(Manager.class));
    }

    @Test
    @DisplayName("createManager: Thất bại khi email đã tồn tại")
    @Order(4)
    void createManager_Fail_EmailAlreadyExists() {
        // Arrange
//        when(communeWardRepository.findById(validRegisterDto.getCommuneWardCode()))
//            .thenReturn(Optional.of(mockCommuneWard));
        when(userRepository.existsByEmail(validRegisterDto.getEmail())).thenReturn(true);
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> managerService.createManager(validRegisterDto));
        
        assertEquals("Mail đã được sử dụng", exception.getMessage());
        verify(managerRepository, never()).save(any(Manager.class));
    }

    @Test
    @DisplayName("createManager: Thất bại khi căn cước công dân đã được sử dụng")
    @Order(5)
    void createManager_Fail_DuplicateCID() {
        // Arrange
        Manager existingManagerWithSameCID = Manager.builder()
            .id(2L)
            .cid(validRegisterDto.getCId())
            .email("other@test.com")
            .build();
        
//        when(communeWardRepository.findById(validRegisterDto.getCommuneWardCode()))
//            .thenReturn(Optional.of(mockCommuneWard));
        when(userRepository.existsByEmail(validRegisterDto.getEmail())).thenReturn(false);
        when(sellerRepository.findByCid(validRegisterDto.getCId())).thenReturn(null);
        when(shipperRepository.findByCid(validRegisterDto.getCId())).thenReturn(null);
        when(managerRepository.findByCid(validRegisterDto.getCId())).thenReturn(existingManagerWithSameCID);
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> managerService.createManager(validRegisterDto));
        
        assertEquals("Căn cước công dân đã được sử dụng", exception.getMessage());
        verify(managerRepository, never()).save(any(Manager.class));
        verify(sellerRepository).findByCid(validRegisterDto.getCId());
        verify(shipperRepository).findByCid(validRegisterDto.getCId());
        verify(managerRepository).findByCid(validRegisterDto.getCId());
    }

    @Test
    @DisplayName("updateManager: Thành công khi cập nhật với dữ liệu hợp lệ")
    @Order(6)
    void updateManager_Success_ValidData() {
        // Arrange
        when(managerRepository.findById(1L)).thenReturn(Optional.of(existingManager));
//        when(communeWardRepository.findById(validEditManagerDto.getCommuneWardCode()))
//            .thenReturn(Optional.of(mockCommuneWard));
        when(userRepository.existsByEmail(validEditManagerDto.getEmail())).thenReturn(false);
        when(sellerRepository.findByCid(validEditManagerDto.getCId())).thenReturn(null);
        when(shipperRepository.findByCid(validEditManagerDto.getCId())).thenReturn(null);
        when(managerRepository.findByCid(validEditManagerDto.getCId())).thenReturn(null);
        
        // Act
        assertDoesNotThrow(() -> managerService.updateManager(1L, validEditManagerDto));
        
        // Assert
        ArgumentCaptor<Manager> managerCaptor = ArgumentCaptor.forClass(Manager.class);
        verify(managerRepository).save(managerCaptor.capture());
        
        Manager updatedManager = managerCaptor.getValue();
        assertEquals(validEditManagerDto.getEmail(), updatedManager.getEmail());
        assertEquals(validEditManagerDto.getFullname(), updatedManager.getFullname());
        assertEquals(validEditManagerDto.getBirthDate(), updatedManager.getBirthDate());
        assertEquals(validEditManagerDto.getCId(), updatedManager.getCid());
        assertEquals(validEditManagerDto.getSpecificAddress(), updatedManager.getSpecificAddress());
        assertTrue(updatedManager.isEnabled());
        
        verify(managerRepository).findById(1L);
//        verify(communeWardRepository).findById(validEditManagerDto.getCommuneWardCode());
        verify(userRepository).existsByEmail(validEditManagerDto.getEmail());
        verify(eventPublisher, never()).publishEvent(any(UserDisabledEvent.class));
    }

    @Test
    @DisplayName("updateManager: Thành công khi giữ nguyên email")
    @Order(7)
    void updateManager_Success_SameEmail() {
        // Arrange
        validEditManagerDto.setEmail(existingManager.getEmail());
        when(managerRepository.findById(1L)).thenReturn(Optional.of(existingManager));
//        when(communeWardRepository.findById(validEditManagerDto.getCommuneWardCode()))
//            .thenReturn(Optional.of(mockCommuneWard));
        when(sellerRepository.findByCid(validEditManagerDto.getCId())).thenReturn(null);
        when(shipperRepository.findByCid(validEditManagerDto.getCId())).thenReturn(null);
        when(managerRepository.findByCid(validEditManagerDto.getCId())).thenReturn(null);
        
        // Act
        assertDoesNotThrow(() -> managerService.updateManager(1L, validEditManagerDto));
        
        // Assert
        verify(managerRepository).save(any(Manager.class));
        verify(userRepository, never()).existsByEmail(anyString());
    }

    @Test
    @DisplayName("updateManager: Thành công khi giữ nguyên căn cước công dân")
    @Order(8)
    void updateManager_Success_SameCID() {
        // Arrange
        validEditManagerDto.setCId(existingManager.getCid());
        when(managerRepository.findById(1L)).thenReturn(Optional.of(existingManager));
//        when(communeWardRepository.findById(validEditManagerDto.getCommuneWardCode()))
//            .thenReturn(Optional.of(mockCommuneWard));
        when(userRepository.existsByEmail(validEditManagerDto.getEmail())).thenReturn(false);
        
        // Act
        assertDoesNotThrow(() -> managerService.updateManager(1L, validEditManagerDto));
        
        // Assert
        verify(managerRepository).save(any(Manager.class));
        verify(sellerRepository, never()).findByCid(anyString());
        verify(shipperRepository, never()).findByCid(anyString());
        verify(managerRepository, never()).findByCid(anyString());
    }

    @Test
    @DisplayName("updateManager: Thành công và publish event khi disable manager")
    @Order(9)
    void updateManager_Success_DisableManager() {
        // Arrange
        validEditManagerDto.setStatus(false);
        when(managerRepository.findById(1L)).thenReturn(Optional.of(existingManager));
//        when(communeWardRepository.findById(validEditManagerDto.getCommuneWardCode()))
//            .thenReturn(Optional.of(mockCommuneWard));
        when(userRepository.existsByEmail(validEditManagerDto.getEmail())).thenReturn(false);
        when(sellerRepository.findByCid(validEditManagerDto.getCId())).thenReturn(null);
        when(shipperRepository.findByCid(validEditManagerDto.getCId())).thenReturn(null);
        when(managerRepository.findByCid(validEditManagerDto.getCId())).thenReturn(null);
        
        // Act
        assertDoesNotThrow(() -> managerService.updateManager(1L, validEditManagerDto));
        
        // Assert
        ArgumentCaptor<Manager> managerCaptor = ArgumentCaptor.forClass(Manager.class);
        verify(managerRepository).save(managerCaptor.capture());
        
        Manager updatedManager = managerCaptor.getValue();
        assertFalse(updatedManager.isEnabled());
        
        ArgumentCaptor<UserDisabledEvent> eventCaptor = ArgumentCaptor.forClass(UserDisabledEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        
        UserDisabledEvent event = eventCaptor.getValue();
        assertEquals(validEditManagerDto.getEmail(), event.email());
    }

    @Test
    @DisplayName("updateManager: Thất bại khi không tìm thấy manager")
    @Order(10)
    void updateManager_Fail_ManagerNotFound() {
        // Arrange
        when(managerRepository.findById(999L)).thenReturn(Optional.empty());
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> managerService.updateManager(999L, validEditManagerDto));
        
        assertEquals("Không tìm thấy quản lý", exception.getMessage());
        verify(managerRepository, never()).save(any(Manager.class));
    }

    @Test
    @DisplayName("updateManager: Thất bại khi không tìm thấy xã")
    @Order(11)
    void updateManager_Fail_CommuneWardNotFound() {
        // Arrange
        when(managerRepository.findById(1L)).thenReturn(Optional.of(existingManager));
//        when(communeWardRepository.findById(validEditManagerDto.getCommuneWardCode()))
//            .thenReturn(Optional.empty());
        
        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> managerService.updateManager(1L, validEditManagerDto));
        
        assertEquals("Không tìm thấy xã", exception.getMessage());
        verify(managerRepository, never()).save(any(Manager.class));
    }

    @Test
    @DisplayName("updateManager: Thất bại khi email mới đã tồn tại")
    @Order(12)
    void updateManager_Fail_EmailAlreadyExists() {
        // Arrange
        when(managerRepository.findById(1L)).thenReturn(Optional.of(existingManager));
//        when(communeWardRepository.findById(validEditManagerDto.getCommuneWardCode()))
//            .thenReturn(Optional.of(mockCommuneWard));
        when(userRepository.existsByEmail(validEditManagerDto.getEmail())).thenReturn(true);
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> managerService.updateManager(1L, validEditManagerDto));
        
        assertEquals("Mail đã được sử dụng", exception.getMessage());
        verify(managerRepository, never()).save(any(Manager.class));
    }

    @Test
    @DisplayName("updateManager: Thất bại khi căn cước công dân đã được sử dụng (seller, shipper, hoặc manager khác)")
    @Order(13)
    void updateManager_Fail_DuplicateCID() {
        // Test case 1: CID đã được seller sử dụng
        Seller existingSeller = new Seller();
        when(managerRepository.findById(1L)).thenReturn(Optional.of(existingManager));
//        when(communeWardRepository.findById(validEditManagerDto.getCommuneWardCode()))
//            .thenReturn(Optional.of(mockCommuneWard));
        when(userRepository.existsByEmail(validEditManagerDto.getEmail())).thenReturn(false);
        when(sellerRepository.findByCid(validEditManagerDto.getCId())).thenReturn(existingSeller);
        
        IllegalArgumentException exception1 = assertThrows(IllegalArgumentException.class, 
            () -> managerService.updateManager(1L, validEditManagerDto));
        assertEquals("Căn cước công dân đã được sử dụng", exception1.getMessage());
        
        // Reset mocks
        reset(managerRepository, communeWardRepository, userRepository, sellerRepository, shipperRepository);
        
        // Test case 2: CID đã được shipper sử dụng
        Shipper existingShipper = new Shipper();
        when(managerRepository.findById(1L)).thenReturn(Optional.of(existingManager));
//        when(communeWardRepository.findById(validEditManagerDto.getCommuneWardCode()))
//            .thenReturn(Optional.of(mockCommuneWard));
        when(userRepository.existsByEmail(validEditManagerDto.getEmail())).thenReturn(false);
        when(sellerRepository.findByCid(validEditManagerDto.getCId())).thenReturn(null);
        when(shipperRepository.findByCid(validEditManagerDto.getCId())).thenReturn(existingShipper);
        
        IllegalArgumentException exception2 = assertThrows(IllegalArgumentException.class, 
            () -> managerService.updateManager(1L, validEditManagerDto));
        assertEquals("Căn cước công dân đã được sử dụng", exception2.getMessage());
        
        // Reset mocks
        reset(managerRepository, communeWardRepository, userRepository, sellerRepository, shipperRepository);
        
        // Test case 3: CID đã được manager khác sử dụng
        Manager anotherManager = Manager.builder()
            .id(2L)
            .cid(validEditManagerDto.getCId())
            .build();
        when(managerRepository.findById(1L)).thenReturn(Optional.of(existingManager));
//        when(communeWardRepository.findById(validEditManagerDto.getCommuneWardCode()))
//            .thenReturn(Optional.of(mockCommuneWard));
        when(userRepository.existsByEmail(validEditManagerDto.getEmail())).thenReturn(false);
        when(sellerRepository.findByCid(validEditManagerDto.getCId())).thenReturn(null);
        when(shipperRepository.findByCid(validEditManagerDto.getCId())).thenReturn(null);
        when(managerRepository.findByCid(validEditManagerDto.getCId())).thenReturn(anotherManager);
        
        IllegalArgumentException exception3 = assertThrows(IllegalArgumentException.class, 
            () -> managerService.updateManager(1L, validEditManagerDto));
        assertEquals("Căn cước công dân đã được sử dụng", exception3.getMessage());
        
        // Verify that save was never called in any scenario
        verify(managerRepository, never()).save(any(Manager.class));
    }
}
