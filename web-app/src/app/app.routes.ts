import { Route } from '@angular/router';
import { FeatureAuth } from 'feature-auth';
import { FeatureContactsComponent } from 'feature-contacts';

export const appRoutes: Route[] = [
  { path: 'auth', component: FeatureAuth },
  { path: 'contacts', component: FeatureContactsComponent },
  { path: 'contacts/info', component: FeatureContactsComponent },
  { path: 'contacts/create', component: FeatureContactsComponent },
  { path: 'contacts/edit', component: FeatureContactsComponent },
  { path: '', pathMatch: 'full', redirectTo: 'auth' },
  { path: '**', redirectTo: 'auth' },
];
