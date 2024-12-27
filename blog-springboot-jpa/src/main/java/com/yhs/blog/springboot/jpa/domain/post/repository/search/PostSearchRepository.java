package com.yhs.blog.springboot.jpa.domain.post.repository.search;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import com.yhs.blog.springboot.jpa.domain.post.repository.search.document.PostDocument;

// Should 조건은 title이나 content 중 하나라도 매칭되면 됨
// must조건은 userId는 반드시 일치해야 함
// minimum_should_match는 should 절에서만 필요. 즉 제목 또는 내용 중 하나라도 매칭되면 됨
// Pageable 객체의 sort 정보가 자동으로 ES sort 절로 변환됨
// 이 안에 Sort절 넣으면 오류 발생
@Repository
public interface PostSearchRepository extends ElasticsearchRepository<PostDocument, String> {

    @Query("""
            {
                "bool": {
                    "should": [
                        {
                            "match": {
                                "title.ngram": {
                                    "query": "?0"
                                }
                            }
                        },
                        {
                            "match": {
                                "content.ngram": {
                                    "query": "?0"
                                }
                            }
                        }
                    ],
                    "minimum_should_match": 1
                }
            }
            """)
    Page<PostDocument> searchByAllForAllUser(String keyword, Pageable pageable);

    @Query("""
            {
                "bool": {
                    "must": [
                        {
                            "match": {
                                "title.ngram": {
                                    "query": "?0"
                                }
                            }
                        }
                    ]
                }
            }
            """)
    Page<PostDocument> searchByTitleForAllUser(String keyword, Pageable pageable);

    @Query("""
            {
                "bool": {
                    "must": [
                        {
                            "match": {
                                "content.ngram": {
                                    "query": "?0"
                                }
                            }
                        }
                    ]
                }
            }
            """)
    Page<PostDocument> searchByContentForAllUser(String keyword, Pageable pageable);

    @Query("""
            {
                "bool": {
                    "must": [
                        {
                            "term": {
                                "userId": "?1"
                            }
                        },
                        {
                            "bool": {
                                "should": [
                                    {
                                        "match": {
                                            "title.ngram": {
                                                "query": "?0"
                                            }
                                        }
                                    },
                                    {
                                        "match": {
                                            "content.ngram": {
                                                "query": "?0"
                                            }
                                        }
                                    }
                                ],
                                "minimum_should_match": 1
                            }
                        }
                    ]
                }
            }
            """)
    Page<PostDocument> searchByAllForSpecificUser(String keyword, String userId, Pageable pageable);

    @Query("""
            {
                "bool": {
                    "must": [
                        {
                            "term": {
                                "userId": "?1"
                            }
                        },
                        {
                            "match": {
                                "title.ngram": {
                                    "query": "?0"
                                }
                            }
                        }
                    ]
                }
            }
            """)
    Page<PostDocument> searchByTitleForSpecificUser(String keyword, String userId, Pageable pageable);

    @Query("""
            {
                "bool": {
                    "must": [
                        {
                            "term": {
                                "userId": "?1"
                            }
                        },
                        {
                            "match": {
                                "content.ngram": {
                                    "query": "?0"
                                }
                            }
                        }
                    ]
                }
            }
            """)
    Page<PostDocument> searchByContentForSpecificUser(String keyword, String userId, Pageable pageable);

    @Query("""
            {
                "bool": {
                    "must": [
                        {
                            "term": {
                                "userId": "?1"
                            }
                        },
                        {
                            "term": {
                                "categoryId": "?2"
                            }
                        },
                        {
                            "bool": {
                                "should": [
                                    {
                                        "match": {
                                            "title.ngram": {
                                                "query": "?0"
                                            }
                                        }
                                    },
                                    {
                                        "match": {
                                            "content.ngram": {
                                                "query": "?0"
                                            }
                                        }
                                    }
                                ],
                                "minimum_should_match": 1
                            }
                        }
                    ]
                }
            }
            """)
    Page<PostDocument> searchByAllAndCategoryForSpecificUser(String keyword, String userId, String categoryId,
            Pageable pageable);

    @Query("""
            {
                "bool": {
                    "must": [
                        {
                            "term": {
                                "userId": "?1"
                            }
                        },
                        {
                            "term": {
                                "categoryId": "?2"
                            }
                        },
                        {
                            "match": {
                                "title.ngram": {
                                    "query": "?0"
                                }
                            }
                        }
                    ]
                }
            }
            """)
    Page<PostDocument> searchByTitleAndCategoryForSpecificUser(String keyword, String userId, String categoryId,
            Pageable pageable);

    @Query("""
            {
                "bool": {
                    "must": [
                        {
                            "term": {
                                "userId": "?1"
                            }
                        },
                        {
                            "term": {
                                "categoryId": "?2"
                            }
                        },
                        {
                            "match": {
                                "content.ngram": {
                                    "query": "?0"
                                }
                            }
                        }
                    ]
                }
            }
            """)
    Page<PostDocument> searchByContentAndCategoryForSpecificUser(String keyword, String userId, String categoryId,
            Pageable pageable);

    // // 자동완성을 위한 메서드 추가
    // @Query("""
    // {
    // "bool": {
    // "should": [
    // {
    // "match": {
    // "content": {
    // "query": "?0",
    // "analyzer": "my_analyzer"
    // }
    // }
    // }
    // ],
    // "minimum_should_match": 1
    // }
    // }
    // """)
    // Page<PostDocument> autoComplete(String keyword, Pageable pageable);

}
