import React, {FC} from "react";
import {Col, Row} from "rsuite";
import {ProductInfo} from "../../types";

export interface ProductCardProps {
  productMetadata?: ProductInfo
}
const ProductCard: FC<ProductCardProps> =  (props) => {

  const truncate = (value: string | undefined): string | undefined => {
    const limit = 100
    if (value && value.length > limit) return value.substring(0, limit)+ "..."
    return value
  }

  return (
      <Row style={{marginBottom: "1rem"}} className="product-card-header">
        <Col xs={8}>
          <img  src={props.productMetadata?.imageUrl}  alt="Shoppiem" style={{maxHeight: '132px'}}/>
        </Col>
        <Col xs={16}>
          <p>
            <small>{truncate(props.productMetadata?.name)}</small>
          </p>
        </Col>
      </Row>
  );
}
export default ProductCard