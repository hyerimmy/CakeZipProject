package com.example.cakezip.controller

import com.example.cakezip.config.BaseResponse
import com.example.cakezip.domain.shop.Shop
import com.example.cakezip.domain.shop.ShopImg
import com.example.cakezip.dto.NewShopReqDto
import com.example.cakezip.dto.ShopDetailInfoDto
import com.example.cakezip.dto.ShopSimpleInfoDto
import com.example.cakezip.repository.ShopImgRepository
import com.example.cakezip.repository.ShopRepository
import com.example.cakezip.service.ShopImgService
import com.example.cakezip.service.ShopService
import com.example.cakezip.service.UploadStoreImgService

import org.springframework.stereotype.Controller
import org.springframework.transaction.annotation.Transactional
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile



@Controller
class ShopController (private val shopService: ShopService, private val shopImgRepository: ShopImgRepository,private val shopImgService: ShopImgService, private val uploadStoreImgService: UploadStoreImgService){
    @GetMapping("/shops/new")
    fun addShop(model: Model):String {
        //TODO : 사장님 추가
        model.addAttribute("form",NewShopReqDto())
        return "addshop"
    }

    @RequestMapping(value = arrayOf("/shops/new"), method = arrayOf(RequestMethod.POST))
    fun postShop(newShopReqDto: NewShopReqDto) : String{
        shopService.addNewShop(newShopReqDto)
        return "redirect:/shops/new" // FIXME : 경로 결정되면 체크하기
    }

    @ResponseBody
    @RequestMapping(value = arrayOf("/image"), method = arrayOf(RequestMethod.POST))
    fun imageShop(@RequestParam images: List<MultipartFile>) : ArrayList<String>{
        var storeUrlList : ArrayList<String> = ArrayList()

        var token:String = uploadStoreImgService.getCloudAPI() // 클라우드 api를 사용하기 위한 곳

        for(image:MultipartFile in images) {
            var url:String = uploadStoreImgService.upload(image, image.originalFilename.toString(), token)
            storeUrlList.add(url)
        }
        return storeUrlList
    }


    @RequestMapping(value = arrayOf("/new/image"), method = arrayOf(RequestMethod.POST))
    fun editImage(@RequestParam images: List<MultipartFile>, @RequestParam shopId : String, model: Model) : String{
        var storeUrlList : ArrayList<String> = ArrayList()

        var token:String = uploadStoreImgService.getCloudAPI() // 클라우드 api를 사용하기 위한 곳

        for(image:MultipartFile in images) {
            var url:String = uploadStoreImgService.upload(image, image.originalFilename.toString(), token)
            storeUrlList.add(url)
        }
        shopImgRepository.save(ShopImg(shopService.getByShopId(shopId.toLong()),storeUrlList.get(0)))

        model.addAttribute("shop", shopService.getByShopId(shopId.toLong()))
        model.addAttribute("shopImg", shopImgService.getShopImgs(shopService.getByShopId(shopId.toLong())))
        return "editimage"
    }


    @GetMapping("/shops")
    fun shopList(model: Model) : String {
        model.addAttribute("shops",shopService.getAllShopSimpleList())
        return "allshop"
    }

    @GetMapping("/shops/{shopId}")
    fun shopDetail(@PathVariable("shopId") shopId:Long, model:Model) : String {
        val shopDetail:ShopDetailInfoDto = shopService.getShopDetail(shopId)
        model.addAttribute("shopInfo",shopDetail)
        return "product"
    }

    @GetMapping("/shops/image/{shopId}")
    fun shopImg(@PathVariable("shopId") shopId:Long, model: Model) : String {

        val shop : Shop = shopService.getByShopId(shopId)

        model.addAttribute("shop", shop)
        model.addAttribute("shopImg", shopImgService.getShopImgs(shop))
        return "editimage"
    }


    @DeleteMapping("/shops/image/{imageId}")
    @Transactional
    fun deleteShopImg(@PathVariable("imageId") imageIds:Long, model:Model) : String {
        val shopImg : ShopImg = shopImgService.findByImgId(imageIds)
        shopImgRepository.deleteByShopImgId(imageIds)

        model.addAttribute("shop", shopImg.shop)
        model.addAttribute("shopImg", shopImgService.getShopImgs(shopImg.shop))
        return "editimage"
    }


}