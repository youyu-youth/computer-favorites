/**
 * English user messages
 *
 * @author yyyouth zg
 * @date 2025-03-23
 */

import type { UserMessages } from '../../types'

export const user: UserMessages = {
  profile: {
    title: 'Profile',
    mySubmissions: 'My Submissions',
    editProfile: 'Edit Profile',
    viewProfile: 'View Profile',
    basicInfo: 'Basic Info',
    nickname: 'Nickname',
    avatar: 'Avatar',
    gender: 'Gender',
    male: 'Male',
    female: 'Female',
    other: 'Other',
    country: 'Country',
    city: 'City',
    signature: 'Signature',
    hobbies: 'Hobbies',
    techStack: 'Tech Stack',
    socialLinks: 'Social Links',
    github: 'GitHub',
    gitee: 'Gitee',
    blog: 'Blog',
    otherRepo: 'Other Repo',
    contribution: 'Contribution',
    contributions: 'contributions',
    noContribution: 'No contribution yet',
    projects: 'Projects',
    websites: 'Websites',
    skills: 'Skills',
    noSkills: 'No skills yet',
  },
  home: {
    hero: {
      title: "Developer's Treasure Collection",
      subtitle: 'Discover, bookmark, and share quality programming resources',
      uploadButton: 'Upload Website',
      browseButton: 'Browse Popular',
    },
    feedback: {
      navButton: 'Feedback',
      dialogTitle: 'Feedback',
      dialogDescription: 'Share suggestions or issues and we will follow up as soon as possible.',
      typeLabel: 'Feedback Type',
      typeSuggestion: 'Suggestion',
      typeBug: 'Bug Report',
      typeComplaint: 'Complaint',
      typeExperience: 'Experience',
      contentLabel: 'Details',
      contentPlaceholder: 'Please describe your suggestion or issue in at least 10 characters.',
      contentCount: '{current} chars entered, min {min}, max {max}',
      contactLabel: 'Contact (Optional)',
      contactPlaceholder: 'Email, phone number, or social account for follow-up.',
      imagesLabel: 'Screenshots (Optional)',
      uploadAction: 'Upload',
      imageAlt: 'Feedback image {index}',
      removeImage: 'Remove image {index}',
      submitAction: 'Submit Feedback',
      submitSuccess: 'Feedback submitted successfully. Thank you!',
      submitFailed: 'Feedback submission failed. Please try again later.',
    },
    filter: {
      search: 'Search',
      searchPlaceholder: 'Search website name or description...',
      categories: 'Categories',
      allCategories: 'All Categories',
      logic: {
        and: 'AND',
        or: 'OR',
      },
    },
    grid: {
      visit: 'Visit',
      copyLink: 'Copy Link',
      noWebsite: 'No websites',
      noUrl: 'No URL available',
    },
  },
}
