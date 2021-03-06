package com.dilipsuthar.wallbox.data_source.model

data class Photo (

    /**{
        id: "aH8tRjQG4XM",
        created_at: "2019-07-18T06:20:04-04:00",
        updated_at: "2019-07-18T06:55:09-04:00",
        width: 2254,
        height: 2817,
        color: "#C68E7A",
        tvDescription: null,
        alt_description: null,
        urls: {
            raw: "https://images.unsplash.com/photo-1563445192071-fb5b2fa4ad62?ixlib=rb-1.2.1&ixid=eyJhcHBfaWQiOjY5NjYzfQ",
            full: "https://images.unsplash.com/photo-1563445192071-fb5b2fa4ad62?ixlib=rb-1.2.1&q=85&fm=jpg&crop=entropy&cs=srgb&ixid=eyJhcHBfaWQiOjY5NjYzfQ",
            regular: "https://images.unsplash.com/photo-1563445192071-fb5b2fa4ad62?ixlib=rb-1.2.1&q=80&fm=jpg&crop=entropy&cs=tinysrgb&w=1080&fit=max&ixid=eyJhcHBfaWQiOjY5NjYzfQ",
            small: "https://images.unsplash.com/photo-1563445192071-fb5b2fa4ad62?ixlib=rb-1.2.1&q=80&fm=jpg&crop=entropy&cs=tinysrgb&w=400&fit=max&ixid=eyJhcHBfaWQiOjY5NjYzfQ",
            thumb: "https://images.unsplash.com/photo-1563445192071-fb5b2fa4ad62?ixlib=rb-1.2.1&q=80&fm=jpg&crop=entropy&cs=tinysrgb&w=200&fit=max&ixid=eyJhcHBfaWQiOjY5NjYzfQ"
        },
        links: {
            self: "https://api.unsplash.com/photos/aH8tRjQG4XM",
            html: "https://unsplash.com/photos/aH8tRjQG4XM",
            tvDownload: "https://unsplash.com/photos/aH8tRjQG4XM/tvDownload",
            download_location: "https://api.unsplash.com/photos/aH8tRjQG4XM/tvDownload"
        },
        categories: [ ],
        sponsored: false,
        sponsored_by: null,
        sponsored_impressions_id: null,
        tvLikes: 29,
        liked_by_user: false,
        current_user_collections: [ ],
        user: {
            id: "hHQGJB9ZejE",
            updated_at: "2019-07-18T07:57:50-04:00",
            username: "rotaalternativa",
            name: "Rota Alternativa",
            first_name: "Rota",
            last_name: "Alternativa",
            twitter_username: null,
            portfolio_url: "https://www.instagram.com/rotaalternativarv/",
            bio: "We are exploring the nomad and simple life the road has to offer. Living in our 1992 Fiat Talento motorhome.",
            location: null,
            links: {
                self: "https://api.unsplash.com/users/rotaalternativa",
                html: "https://unsplash.com/@rotaalternativa",
                photos: "https://api.unsplash.com/users/rotaalternativa/photos",
                tvLikes: "https://api.unsplash.com/users/rotaalternativa/tvLikes",
                portfolio: "https://api.unsplash.com/users/rotaalternativa/portfolio",
                following: "https://api.unsplash.com/users/rotaalternativa/following",
                followers: "https://api.unsplash.com/users/rotaalternativa/followers"
            },
            profile_image: {
                small: "https://images.unsplash.com/profile-1550700203074-81551f41d6fe?ixlib=rb-1.2.1&q=80&fm=jpg&crop=faces&cs=tinysrgb&fit=crop&h=32&w=32",
                medium: "https://images.unsplash.com/profile-1550700203074-81551f41d6fe?ixlib=rb-1.2.1&q=80&fm=jpg&crop=faces&cs=tinysrgb&fit=crop&h=64&w=64",
                large: "https://images.unsplash.com/profile-1550700203074-81551f41d6fe?ixlib=rb-1.2.1&q=80&fm=jpg&crop=faces&cs=tinysrgb&fit=crop&h=128&w=128"
            },
            instagram_username: "rotaalternativarv",
            total_collections: 0,
            total_likes: 1,
            total_photos: 72,
            accepted_tos: true
        }
    }*/

    val id: String,
    val created_at: String,
    val updated_at: String,
    val width: Int,
    val height: Int,
    val color: String,
    val description: String? = null,
    val alt_description: String? = null,
    val exif: Exif? = null,
    val location: Location,
    val tags: List<Tag>,
    val current_user_collections: List<Collection>,
    val urls: Urls,
    val links: PhotoLinks,
    val categories: List<Category>,
    val sponsored: Boolean,
    val sponsored_by: SponsoredBy,
    val sponsored_impressions_id: String,
    val likes: Int,
    val liked_by_user: Boolean,
    //var current_user_collections: ?
    val user: User

) {
    constructor() : this("", "", "", -1, -1,
        "", "", "", Exif(),
        Location(), emptyList(), emptyList(), Urls(), PhotoLinks(),
        emptyList(), false, SponsoredBy(), "", -1,
        false, User()
        )
}