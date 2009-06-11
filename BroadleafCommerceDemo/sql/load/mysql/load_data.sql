SET foreign_key_checks = 0;
set sql_mode = '';


DELETE FROM broadleafcommerce.BLC_CATEGORY;

LOAD DATA INFILE '@@BASE_DIR@@/category.txt' 
INTO TABLE broadleafcommerce.BLC_CATEGORY
FIELDS TERMINATED BY '|' ENCLOSED BY "" 
(CATEGORY_ID, DESCRIPTION, DISPLAY_TEMPLATE, LONG_DESCRIPTION, NAME, URL, URL_KEY, DEFAULT_PARENT_CATEGORY_ID)
  SET ACTIVE_END_DATE = DATE_ADD(CURDATE(), INTERVAL 31 DAY),
  ACTIVE_START_DATE = CURDATE();

UPDATE broadleafcommerce.BLC_CATEGORY
SET DEFAULT_PARENT_CATEGORY_ID = null
WHERE DEFAULT_PARENT_CATEGORY_ID = 0;

DELETE FROM broadleafcommerce.BLC_PRODUCT;

LOAD DATA INFILE '@@BASE_DIR@@/product.txt' 
INTO TABLE broadleafcommerce.BLC_PRODUCT
FIELDS TERMINATED BY '|' ENCLOSED BY "" 
(PRODUCT_ID, DEFAULT_CATEGORY_ID, NAME, DESCRIPTION, IS_FEATURED_PRODUCT)
  SET ACTIVE_END_DATE = DATE_ADD(CURDATE(), INTERVAL 31 DAY),
  ACTIVE_START_DATE = CURDATE();
  
DELETE FROM broadleafcommerce.BLC_CATEGORY_XREF;

LOAD DATA INFILE '@@BASE_DIR@@/category_xref.txt' 
INTO TABLE broadleafcommerce.BLC_CATEGORY_XREF
FIELDS TERMINATED BY '|' ENCLOSED BY "" 
(SUB_CATEGORY_ID, CATEGORY_ID, DISPLAY_ORDER);

DELETE FROM broadleafcommerce.BLC_CATEGORY_PRODUCT_XREF;

LOAD DATA INFILE '@@BASE_DIR@@/category_product_xref.txt' 
INTO TABLE broadleafcommerce.BLC_CATEGORY_PRODUCT_XREF
FIELDS TERMINATED BY '|' ENCLOSED BY "" 
(PRODUCT_ID, CATEGORY_ID, DISPLAY_ORDER);

DELETE FROM broadleafcommerce.BLC_SKU;

LOAD DATA INFILE '@@BASE_DIR@@/sku.txt' 
INTO TABLE broadleafcommerce.BLC_SKU
FIELDS TERMINATED BY '|' ENCLOSED BY "" 
(SKU_ID, NAME, DESCRIPTION, RETAIL_PRICE, SALE_PRICE, TAXABLE_FLAG, DISCOUNTABLE_FLAG)
  SET ACTIVE_END_DATE = DATE_ADD(CURDATE(), INTERVAL 31 DAY),
  ACTIVE_START_DATE = CURDATE();

DELETE FROM broadleafcommerce.BLC_PRODUCT_SKU_XREF;

LOAD DATA INFILE '@@BASE_DIR@@/product_sku_xref.txt' 
INTO TABLE broadleafcommerce.BLC_PRODUCT_SKU_XREF
FIELDS TERMINATED BY '|' ENCLOSED BY ""; 

DELETE FROM broadleafcommerce.BLC_PRODUCT_ATTRIBUTE;

LOAD DATA INFILE '@@BASE_DIR@@/product_attribute.txt' 
INTO TABLE broadleafcommerce.BLC_PRODUCT_ATTRIBUTE
FIELDS TERMINATED BY '|' ENCLOSED BY "" 
(ID, PRODUCT_ID, NAME, VALUE, SEARCHABLE);

DELETE FROM broadleafcommerce.BLC_PRODUCT_IMAGE;

LOAD DATA INFILE '@@BASE_DIR@@/product_image.txt' 
INTO TABLE broadleafcommerce.BLC_PRODUCT_IMAGE
FIELDS TERMINATED BY '|' ENCLOSED BY ""
(PRODUCT_ID, URL, NAME);

SET foreign_key_checks = 1;


