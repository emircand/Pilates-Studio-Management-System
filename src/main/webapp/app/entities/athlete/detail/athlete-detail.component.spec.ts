import { TestBed } from '@angular/core/testing';
import { provideRouter, withComponentInputBinding } from '@angular/router';
import { RouterTestingHarness, RouterTestingModule } from '@angular/router/testing';
import { of } from 'rxjs';

import { AthleteDetailComponent } from './athlete-detail.component';

describe('Athlete Management Detail Component', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AthleteDetailComponent, RouterTestingModule.withRoutes([], { bindToComponentInputs: true })],
      providers: [
        provideRouter(
          [
            {
              path: '**',
              component: AthleteDetailComponent,
              resolve: { athlete: () => of({ id: 123 }) },
            },
          ],
          withComponentInputBinding(),
        ),
      ],
    })
      .overrideTemplate(AthleteDetailComponent, '')
      .compileComponents();
  });

  describe('OnInit', () => {
    it('Should load athlete on init', async () => {
      const harness = await RouterTestingHarness.create();
      const instance = await harness.navigateByUrl('/', AthleteDetailComponent);

      // THEN
      expect(instance.athlete).toEqual(expect.objectContaining({ id: 123 }));
    });
  });
});
