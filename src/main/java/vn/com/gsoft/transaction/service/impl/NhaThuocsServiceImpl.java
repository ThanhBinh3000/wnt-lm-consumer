package vn.com.gsoft.transaction.service.impl;


import com.google.gson.Gson;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.TypeToken;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import vn.com.gsoft.transaction.constant.RecordStatusContains;
import vn.com.gsoft.transaction.constant.StatusConstant;
import vn.com.gsoft.transaction.entity.LichSuCapNhatThanhVien;
import vn.com.gsoft.transaction.entity.NhaThuocs;
import vn.com.gsoft.transaction.entity.UserProfile;
import vn.com.gsoft.transaction.model.dto.NhaThuocsReq;
import vn.com.gsoft.transaction.model.dto.WrapData;
import vn.com.gsoft.transaction.repository.LichSuCapNhatThanhVienRepository;
import vn.com.gsoft.transaction.repository.NhaThuocsRepository;
import vn.com.gsoft.transaction.repository.UserProfileRepository;
import vn.com.gsoft.transaction.service.NhaThuocsService;
import vn.com.gsoft.transaction.constant.ObjectTypeConstant;

import java.util.Date;

@Service
@Log4j2
public class NhaThuocsServiceImpl implements NhaThuocsService {


    @Autowired
    private NhaThuocsRepository hdrRepo;

    @Autowired
    private UserProfileRepository userProfileRepository;
    @Autowired
    private LichSuCapNhatThanhVienRepository lichSuCapNhatThanhVienRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void updateData(String payload) {
        Gson gson = new Gson();
        TypeToken<WrapData<NhaThuocsReq>> typeToken = new TypeToken<WrapData<NhaThuocsReq>>() {};
        WrapData<NhaThuocsReq> data = gson.fromJson(payload, typeToken.getType());
        NhaThuocsReq item = data.getData();
        switch (item.getType()){
            case ObjectTypeConstant.NHA_THUOC -> {
                if(item.getTypeUpdate() == StatusConstant.ADD){
                    createInfo(item);
                } if(item.getTypeUpdate() == StatusConstant.UPDATE){
                    updateInfo(item);
                }if(item.getTypeUpdate() == StatusConstant.DELETE){
                    deleteByMaNhaThuoc(item);
                }
                break;
            }
            case ObjectTypeConstant.NGUOI_DUNG -> {
                if(item.getTypeUpdate() == StatusConstant.ADD){
                    createUser(item);
                } if(item.getTypeUpdate() == StatusConstant.UPDATE){
                    updateUser(item);
                }if(item.getTypeUpdate() == StatusConstant.CHANGE_PASS){
                    changePassUser(item);
                }
                break;
            }
        }
    }

    private void createInfo(NhaThuocsReq req){
        NhaThuocs nhaThuoc = new NhaThuocs();
        nhaThuoc.setMaNhaThuoc(req.getMaNhaThuoc());
        nhaThuoc.setCreated(new Date());
        nhaThuoc.setRecordStatusId(RecordStatusContains.ACTIVE);
        nhaThuoc.setEntityId(req.getEntityId());
        BeanUtils.copyProperties(req, nhaThuoc ,"created", "createdByUserId",
                "recordStatusId", "entityId");
        hdrRepo.save(nhaThuoc);
        //lưu lịch sử
        LichSuCapNhatThanhVien lichSuCapNhatThanhVien = new LichSuCapNhatThanhVien();
        lichSuCapNhatThanhVien.setStatusId(StatusConstant.ADD);
        lichSuCapNhatThanhVien.setMaThanhVien(req.getMaNhaThuoc());
        lichSuCapNhatThanhVien.setGhiChu(req.getDescription() != null ? req.getDescription() : StatusConstant.ADD_TXT);
        lichSuCapNhatThanhVien.setNgayCapNhat(new Date());
        lichSuCapNhatThanhVienRepository.save(lichSuCapNhatThanhVien);
    }

    private void updateInfo(NhaThuocsReq req) {
        NhaThuocs nhaThuoc = hdrRepo.findByMaNhaThuoc(req.getMaNhaThuoc());
        nhaThuoc.setTenNhaThuoc(req.getTenNhaThuoc());
        nhaThuoc.setEntityId(req.getEntityId());
        nhaThuoc.setEmail(req.getEmail());
        nhaThuoc.setDiaChi(req.getDiaChi());
        nhaThuoc.setCityId(req.getCityId() > 0 ? req.getCityId() : 0L);
        nhaThuoc.setRegionId(req.getRegionId() > 0 ? req.getRegionId() : 0L);
        nhaThuoc.setWardId(req.getWardId() > 0 ? req.getWardId() : 0L);
        nhaThuoc.setModified(new Date());

        hdrRepo.save(nhaThuoc);
        //update userprofile
        //lưu lịch sử
        LichSuCapNhatThanhVien lichSuCapNhatThanhVien = new LichSuCapNhatThanhVien();
        lichSuCapNhatThanhVien.setStatusId(StatusConstant.UPDATE);
        lichSuCapNhatThanhVien.setMaThanhVien(req.getMaNhaThuoc());
        lichSuCapNhatThanhVien.setGhiChu(req.getDescription() != null ? req.getDescription() : StatusConstant.UPDATE_TXT);
        lichSuCapNhatThanhVien.setNgayCapNhat(new Date());
        lichSuCapNhatThanhVienRepository.save(lichSuCapNhatThanhVien);
    }

    private void deleteByMaNhaThuoc(NhaThuocsReq req){
        NhaThuocs nhaThuoc = hdrRepo.findByMaNhaThuoc(req.getMaNhaThuoc());
        nhaThuoc.setRecordStatusId(RecordStatusContains.DELETED);
        nhaThuoc.setModified(new Date());
        hdrRepo.save(nhaThuoc);
        //lưu lịch sử
        LichSuCapNhatThanhVien lichSuCapNhatThanhVien = new LichSuCapNhatThanhVien();
        lichSuCapNhatThanhVien.setStatusId(StatusConstant.DELETE);
        lichSuCapNhatThanhVien.setMaThanhVien(req.getMaNhaThuoc());
        lichSuCapNhatThanhVien.setGhiChu(req.getDescription() != null ? req.getDescription() : StatusConstant.DELETE_TXT);
        lichSuCapNhatThanhVien.setNgayCapNhat(new Date());
        lichSuCapNhatThanhVienRepository.save(lichSuCapNhatThanhVien);
    }

    private void createUser(NhaThuocsReq req){
        //lưu tài khoản
        UserProfile userProfile = new UserProfile();
        userProfile.setMaNhaThuoc(req.getMaNhaThuoc());
        userProfile.setUserName(req.getUserName());
        userProfile.setPassword(passwordEncoder.encode(req.getPassword()));
        userProfile.setTenDayDu(req.getTenNhaThuoc());
        userProfile.setCreated(new Date());
        userProfile.setHoatDong(true);
        userProfile.setUserId(0L);
        userProfile.setEntityId(Long.valueOf(req.getEntityId()));
        userProfileRepository.save(userProfile);
    }

    private void updateUser(NhaThuocsReq req){
        var userProfiles = userProfileRepository.findByUserId(req.getUserId());
        if(userProfiles != null){
            var user = new UserProfile();
            BeanUtils.copyProperties(userProfiles.get(), user);
            user.setEntityId(Long.valueOf(req.getEntityId()));
            user.setTenDayDu(req.getFullName());
            userProfileRepository.save(user);
        }
    }

    private void changePassUser(NhaThuocsReq req){
        var userProfiles = userProfileRepository.findByUserId(req.getUserId());
        if(userProfiles != null){
            var user = new UserProfile();
            BeanUtils.copyProperties(userProfiles.get(), user);
            user.setPassword(passwordEncoder.encode(req.getPassword()));
            userProfileRepository.save(user);
        }
    }
    //endregion
}
