package com.xuwei.service.Impl;

import com.xuwei.config.JwtProvider;
import com.xuwei.domain.AccountStatus;
import com.xuwei.domain.USER_ROLE;
import com.xuwei.model.Address;
import com.xuwei.model.Seller;
import com.xuwei.repository.AddressRepository;
import com.xuwei.repository.SellerRepository;
import com.xuwei.service.SellerService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Seller service implementation handling seller operations including:
 * - Seller profile management
 * - Seller registration and account creation
 * - Seller information updates
 * - Account status management
 */
@Service
@RequiredArgsConstructor
public class SellerServiceImpl implements SellerService {
    private final SellerRepository sellerRepository;
    private final JwtProvider jwtProvider;
    private final PasswordEncoder passwordEncoder;
    private final AddressRepository addressRepository;

    /**
     * Retrieves seller profile using JWT token
     * @param jwt JWT token containing seller email
     * @return seller profile information
     */
    @Override
    public Seller getSellerProfile(String jwt) {
        String email = jwtProvider.getEmailFromJwtToken(jwt);
        return sellerRepository.findByEmail(email);
    }

    /**
     * Creates a new seller account with validation
     * @param seller seller information to create
     * @return created seller entity
     * @throws Exception if seller with email already exists
     */
    @Override
    public Seller createSeller(Seller seller) throws Exception {
        validateSellerEmailNotExists(seller.getEmail());

        Address savedAddress = addressRepository.save(seller.getPickupAddress());
        Seller newSeller = buildNewSeller(seller, savedAddress);

        return sellerRepository.save(newSeller);
    }

    /**
     * Retrieves seller by ID
     * @param id seller ID
     * @return seller entity
     * @throws Exception if seller not found
     */
    @Override
    public Seller getSellerById(Long id) throws Exception {
        return sellerRepository.findById(id)
                .orElseThrow(() -> new Exception("Seller not found with id: " + id));
    }

    /**
     * Retrieves seller by email address
     * @param email seller email
     * @return seller entity
     * @throws Exception if seller not found
     */
    @Override
    public Seller getSellerByEmail(String email) throws Exception {
        return Optional.ofNullable(sellerRepository.findByEmail(email))
                .orElseThrow(() -> new Exception("Seller not found with email: " + email));
    }

    /**
     * Retrieves all sellers filtered by account status
     * @param status account status filter
     * @return list of sellers
     */
    @Override
    public List<Seller> getAllSellers(AccountStatus status) {
        if (status == null) {
            return sellerRepository.findAll();
        } else {
            return sellerRepository.findByAccountStatus(status);
        }
    }


    /**
     * Updates seller information with partial updates
     * Only non-null fields will be updated
     * @param id seller ID
     * @param seller updated seller information
     * @return updated seller entity
     * @throws Exception if seller not found
     */
    @Override
    public Seller updateSeller(Long id, Seller seller) throws Exception {
        Seller existingSeller = getSellerById(id);

        updateBasicInfo(existingSeller, seller);
        updateBusinessDetails(existingSeller, seller);
        updateBankDetails(existingSeller, seller);
        updateAddressDetails(existingSeller, seller);

        return sellerRepository.save(existingSeller);
    }

    /**
     * Deletes seller by ID
     * @param id seller ID to delete
     * @throws Exception if seller not found
     */
    @Override
    public void deleteSeller(Long id) throws Exception {
        if (!sellerRepository.existsById(id)) {
            throw new Exception("Seller not found with id: " + id);
        }
        sellerRepository.deleteById(id);
    }

    /**
     * Verifies seller email using OTP
     * @param email seller email to verify
     * @param otp one-time password for verification
     * @return updated seller with verified email
     * @throws Exception if seller not found
     */
    @Override
    public Seller verifyEmail(String email, String otp) throws Exception {
        Seller seller = getSellerByEmail(email);
        seller.setEmailVerified(true);
        return sellerRepository.save(seller);
    }

    /**
     * Updates seller account status (active, suspended, etc.)
     * @param sellerId seller ID
     * @param status new account status
     * @return updated seller entity
     * @throws Exception if seller not found
     */
    @Override
    public Seller updateSellerAccountStatus(Long sellerId, AccountStatus status) throws Exception {
        Seller seller = getSellerById(sellerId);
        seller.setAccountStatus(status);
        return sellerRepository.save(seller);
    }

    // ============ PRIVATE HELPER METHODS ============

    /**
     * Validates that no seller exists with the given email
     * @param email email to check
     * @throws Exception if seller with email already exists
     */
    private void validateSellerEmailNotExists(String email) throws Exception {
        Seller existingSeller = sellerRepository.findByEmail(email);
        if (existingSeller != null) {
            throw new Exception("Seller already exists with email: " + email);
        }
    }

    /**
     * Builds new seller entity from request data
     * @param seller source seller data
     * @param savedAddress persisted address entity
     * @return new seller entity ready for persistence
     */
    private Seller buildNewSeller(Seller seller, Address savedAddress) {
        Seller newSeller = new Seller();
        newSeller.setEmail(seller.getEmail());
        newSeller.setPickupAddress(savedAddress);
        newSeller.setSellerName(seller.getSellerName());
        newSeller.setGSTIN(seller.getGSTIN());
        newSeller.setRole(USER_ROLE.ROLE_SELLER);
        newSeller.setPhone(seller.getPhone());
        newSeller.setPassword(passwordEncoder.encode(seller.getPassword()));
        newSeller.setBankDetails(seller.getBankDetails());
        newSeller.setBusinessDetails(seller.getBusinessDetails());

        return newSeller;
    }

    /**
     * Updates basic seller information
     * @param existingSeller seller entity to update
     * @param updatedData new seller data
     */
    private void updateBasicInfo(Seller existingSeller, Seller updatedData) {
        if (updatedData.getSellerName() != null) {
            existingSeller.setSellerName(updatedData.getSellerName());
        }
        if (updatedData.getPhone() != null) {
            existingSeller.setPhone(updatedData.getPhone());
        }
        if (updatedData.getEmail() != null) {
            existingSeller.setEmail(updatedData.getEmail());
        }
        if (updatedData.getGSTIN() != null) {
            existingSeller.setGSTIN(updatedData.getGSTIN());
        }
    }

    /**
     * Updates business details if provided
     * @param existingSeller seller entity to update
     * @param updatedData new seller data
     */
    private void updateBusinessDetails(Seller existingSeller, Seller updatedData) {
        if (updatedData.getBusinessDetails() == null) {
            return;
        }

        var existing = existingSeller.getBusinessDetails();
        var updated = updatedData.getBusinessDetails();

        if (updated.getBusinessName() != null) {
            existing.setBusinessName(updated.getBusinessName());
        }
        if (updated.getBusinessEmail() != null) {
            existing.setBusinessEmail(updated.getBusinessEmail());
        }
        if (updated.getBusinessPhone() != null) {
            existing.setBusinessPhone(updated.getBusinessPhone());
        }
        if (updated.getBusinessAddress() != null) {
            existing.setBusinessAddress(updated.getBusinessAddress());
        }
        if (updated.getLogo() != null) {
            existing.setLogo(updated.getLogo());
        }
        if (updated.getBanner() != null) {
            existing.setBanner(updated.getBanner());
        }
    }


    /**
     * Updates bank details if all required fields are provided
     * @param existingSeller seller entity to update
     * @param updatedData new seller data
     */
    private void updateBankDetails(Seller existingSeller, Seller updatedData) {
        if (updatedData.getBankDetails() != null &&
                updatedData.getBankDetails().getAccountHolderName() != null &&
                updatedData.getBankDetails().getIfscCode() != null &&
                updatedData.getBankDetails().getAccountNumber() != null) {

            existingSeller.getBankDetails().setAccountHolderName(
                    updatedData.getBankDetails().getAccountHolderName()
            );
            existingSeller.getBankDetails().setAccountNumber(
                    updatedData.getBankDetails().getAccountNumber()
            );
            existingSeller.getBankDetails().setIfscCode(
                    updatedData.getBankDetails().getIfscCode()
            );
        }
    }

    /**
     * Updates address details if all required fields are provided
     * @param existingSeller seller entity to update
     * @param updatedData new seller data
     */
    private void updateAddressDetails(Seller existingSeller, Seller updatedData) {
        if (updatedData.getPickupAddress() != null &&
                updatedData.getPickupAddress().getAddress() != null &&
                updatedData.getPickupAddress().getPhone() != null &&
                updatedData.getPickupAddress().getCity() != null &&
                updatedData.getPickupAddress().getState() != null) {

            Address address = existingSeller.getPickupAddress();
            address.setAddress(updatedData.getPickupAddress().getAddress());
            address.setCity(updatedData.getPickupAddress().getCity());
            address.setState(updatedData.getPickupAddress().getState());
            address.setPhone(updatedData.getPickupAddress().getPhone());
            address.setPinCode(updatedData.getPickupAddress().getPinCode());
        }
    }
}