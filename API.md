# Bearer Token:
84167d04-c4e0-4d6f-953a-25392801f985

# Post Request:
https://api.brightdata.com/datasets/v3/scrape?dataset_id=gd_l7q7dkf244hwjntr0&custom_output_fields=title%2Cdescription%2Cfinal_price%2Casin%2Cproduct_dimensions%2Citem_weight%2Croot_bs_category%2Cimage_url&notify=false&type=discover_new&discover_by=best_sellers_url&limit_per_input=5

# JSON Body:
{
    "input": [
        {
            "category_url": "https://www.amazon.com/gp/bestsellers/beauty"
        }
    ]
}

# GET Request Change Snapshot Token:
https://api.brightdata.com/datasets/v3/snapshot/sd_mk3g6dufo4yub6hdy

# GET Request with NoAuth:
localhost:8082/api/skus/add-category

# Body:
{
    "input": [
        {
            "category_url": "https://www.amazon.com/gp/bestsellers/home-garden"
        }
    ]
}