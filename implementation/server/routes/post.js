var Post = require('../models/post')
    , User = require('../models/user')
    , Token = require('../models/token')
    , utils = require('../models/utils')
    , constants = require('../models/constants')
    , rs = require('random-strings');

exports.createPost = function(req, res) {
    var accessToken = utils.getAccessToken(req.headers.authorization);
    Token.findOne({ accessToken: accessToken }, function(err, tokenRecord) {
        if (err) { res.send(err) }
        else {
            var post = new Post({
                userId: tokenRecord.userId,
                type: req.body.type,
                location: [req.body.lat, req.body.long],
                images: req.body.images,
                metadata: {
                    specie: req.body.specie,
                    size: req.body.size,
                    color: req.body.color,
                    age: req.body.age,
                    observations: req.body.observations,
                    identification: req.body.identification
                }
            });

            post.save(function (err) {
                if (err) { res.send(err) }
                else { res.json({"valid": "ok"}) }
            });
        }
    });
};

exports.getPost = function(req, res) {
    Post.findOne({ postId: req.params.post_id }, function(err, post) {
       if(err) { res.send(err) }
        else { res.send(post) }
    });
};

exports.getAnnouncer = function(req, res) {
    Post.findOne({ postId: req.params.post_id }, function(err, post) {
        if(err) { res.send(err) }
        else {
            User.findOne({ userId: post.userId }, function(err, user) {
                if(err) { res.send(err) }
                    else { res.send({"phoneNumber": user.phoneNumber}) }
                });
        }
    });
};

exports.getPosts = function(req, res) {
    Post.find({ }, function(err, posts) {
        if(err) { res.send(err) }
        else { res.send(posts) }
    });
};

exports.myPosts = function(req, res) {
    var accessToken = utils.getAccessToken(req.headers.authorization)
    Token.findOne({ accessToken: accessToken }, function(err, tokenRecord) {
        if(err) { res.send(err) }
        else {
            Post.find({ userId: tokenRecord.userId }).sort({publicationDate: 'desc'}).exec(function (err, posts) {
                if (err) { res.send(err) }
                else { res.send(posts) }
            });
        }
    });
};

exports.removePost = function(req, res) {
    var accessToken = utils.getAccessToken(req.headers.authorization)
    Token.findOne({ accessToken: accessToken }, function(err, tokenRecord) {
        if(err) { res.send(err) }
        else {
            Post.remove({ userId: tokenRecord.userId, postId: req.params.post_id }, function(err) {
                if (err){ res.send(err) }
                res.json({ "valid": "ok" });
            });
        }
    });
};

exports.findPost = function(req, res) {

    if (!req.body.type || !req.body.lat || !req.body.long || !req.body.specie) {
        res.send({});
        return
    }

    var query = Post.find({
        'location': {$near: [req.body.lat, req.body.long], $maxDistance: POST_GEODISTANCE},
        'metadata.specie': req.body.specie
    });

    query.where('type').ne(req.body.type);

    if (req.body.size) { query.where('metadata.size').equals(req.body.size); }

    if (req.body.color) { query.where('metadata.color').in(req.body.color); }

    query.sort({publicationDate: 'desc'});
    query.exec(function(err, posts){
        if(err) { res.send(err) }
        else { res.send(posts) }
    });
};
