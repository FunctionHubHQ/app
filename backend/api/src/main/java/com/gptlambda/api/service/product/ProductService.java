package com.gptlambda.api.service.product;

import com.gptlambda.api.ProductCreateResponse;
import com.gptlambda.api.ProductFromDataRequest;
import com.gptlambda.api.ProductRequest;
import com.gptlambda.api.data.postgres.entity.ProductEntity;

/**
 * @author Biz Melesse created on 5/25/23
 */
public interface ProductService {
  ProductCreateResponse createProductFromData(ProductFromDataRequest productFromDataRequest);
  ProductCreateResponse createProduct(ProductRequest productRequest, String fcmToken);

  void sendProductInfoToClient(ProductEntity entity, String productSku, String fcmToken);
  String parseProductSku(String productUrl);
}
