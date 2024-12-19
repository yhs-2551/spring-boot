package com.yhs.blog.springboot.jpa.domain.post.repository.search;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

// Should 조건은 title이나 content 중 하나라도 매칭되면 됨
// must조건은 userId는 반드시 일치해야 함
// minimum_should_match는 should 절에서만 필요. 즉 제목 또는 내용 중 하나라도 매칭되면 됨
@Repository
public interface PostSearchRepository extends ElasticsearchRepository<PostDocument, String> {
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
    Page<PostDocument> searchByAll(String keyword, String userId, Pageable pageable);

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
    Page<PostDocument> searchByTitle(String keyword, String userId, Pageable pageable);

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
    Page<PostDocument> searchByContent(String keyword, String userId, Pageable pageable);

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
    Page<PostDocument> searchByAllAndCategory(String keyword, String userId, String categoryId, Pageable pageable);

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
    Page<PostDocument> searchByTitleAndCategory(String keyword, String userId, String categoryId, Pageable pageable);

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
    Page<PostDocument> searchByContentAndCategory(String keyword, String userId, String categoryId, Pageable pageable);

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
